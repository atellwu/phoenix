function add_ssh_private_key {
	ssh_config=~/.ssh/config
	local host_cnt=`grep -c $git_host $ssh_config 2>/dev/null || true`
	local write=0
	if [ x$host_cnt == x ];then	#config file not exist
		log ".ssh/config not found"
		write=1
	else
		if [ $host_cnt -eq 0 ];then	#no config entry for git host
			log "no phoenix-load-balancer private key found in .ssh/config"
			write=1
		fi
	fi
	if [ $write -eq 1  ];then
		log "try to add phoenix-load-balancer private key to .ssh/config"
		mkdir -p ~/.ssh
		cp -r lbgit/.ssh/id_rsa ~/.ssh/id_rsa.phoenixlb
		chmod 600 ~/.ssh/id_rsa.phoenixlb
		cat <<-END >> $ssh_config
			
			Host $git_host
			IdentityFile ~/.ssh/id_rsa.phoenixlb
			StrictHostKeyChecking no
		END
		chmod 600 $ssh_config
		log "phoenix-load-balancer private key added to .ssh/config"
	fi
}

function init {
	local config_dir="$tengine_config_doc_base/$virtual_server_name"
	log "initializing $config_dir"
	if [[ ! -d "$config_dir" ]]; then
		mkdir -p "$config_dir"
		log "$config_dir created"
	fi
	
	if [[ -d "$tengine_config_git_doc_base" ]]; then
		rm -rf "$tengine_config_git_doc_base"
		mkdir -p "$tengine_config_git_doc_base"
		log "$tengine_config_git_doc_base reinitialized"
	fi
	
	if [[ ! -d "$config_dir/.git" || ! -f "$config_dir/.git/index" ]];then
		log "no .git directory found in $config_dir, make it a git repo"
		cd "$config_dir"
		git init
		cd - > /dev/null
		log "$config_dir directory now a git repo"
	fi
	
	cd "$config_dir"
	git add -A
	if [ `git status --short | wc -l` != 0 ]; then
		git commit -m "init commit"
	fi
	cd - > /dev/null
}

function git_pull {
	log "getting tengine config from $git_url"

	add_ssh_private_key

	mkdir -p $tengine_config_git_doc_base

	cd $tengine_config_git_doc_base
	if [ -e $tengine_config_git_doc_base/.git ];then
		log "found existing tengine config, fetching tengine config"
		git reset --hard
		git fetch --tags $git_url master
		git checkout $version
	else
		log "no existing tengine config found, cloning tengine config"
		git clone $git_url $tengine_config_git_doc_base
		git checkout $version
	fi
	cd - >/dev/null

	log "got tengine config from git"
}

function copy_config {
	local to_dir="$tengine_config_doc_base/$virtual_server_name"
	local from_dir="$tengine_config_git_doc_base/$virtual_server_name"
	log "copying $config_file from $from_dir to $to_dir"

	cp "$from_dir/$config_file" "$to_dir/$config_file"
	
	log "config copied"
}

function reload_or_dynamic_refresh_config {
	if [ `pgrep nginx | wc -l` != 0 ]; then
		if [ $tengine_reload -eq 1  ];then
			log "reload tengine config"
			sudo -u root /etc/init.d/nginx -s reload || { log_error "fail to reload tengine, exit code is $?"; exit 1; }
	
		else
			log "curling $dynamic_refresh_url with request method $refresh_method(post data: $dynamic_refresh_post_data)"
			local response=`curl -X$refresh_method -d"$dynamic_refresh_post_data" $dynamic_refresh_url`
			if [ "$response"x != "sucess"x ];then
				log "fail to curl"
				exit 1
			fi
		fi
	else
		sudo -u root /etc/init.d/nginx || { log_error "fail to start tengine, exit code is $?"; exit 1; }
	fi
}

# Rollback a git directory
# Parameters: 1. git_directory
function git_rollback {
	local git_dir=$1
	cd $git_dir
	git reset --hard
	cd - >/dev/null
}

function rollback {
	local config_dir="$tengine_config_doc_base/$virtual_server_name"
	# rollback tengine
	log "rolling back tengine config"
	rm -rf $config_dir/*
	git_rollback $config_dir
	log "tengine config version $version rolled back"
}

# Commit a git directory
# Parameters: 1. git_directory, 2. comment
function git_commit {
	local git_dir=$1
	local comment=$2
	cd $git_dir
	
	cat <<-END > .version
			$version
	END
	
	local change_files=`git status --short | wc -l`
	if [ $change_files -gt 0 ];then
		log "committing $change_files files"
		git add -A
		git commit -m "$comment"
	else
		log "no file changed, no commit necessary"
	fi
	cd - >/dev/null
}

function commit {
	# commit tengine config
	local config_dir="$tengine_config_doc_base/$virtual_server_name"
	log "committing tengine config for $virtual_server_name in $config_dir"
	git_commit $config_dir "update to $version"
	log "committed"
}

