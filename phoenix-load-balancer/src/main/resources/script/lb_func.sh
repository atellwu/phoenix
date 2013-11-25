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
		cp -r git/.ssh/id_rsa ~/.ssh/id_rsa.phoenixlb
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

function git_clone {
	log "cloning from $git_url to $target_dir"

	add_ssh_private_key

	if [[ -d "$target_dir" ]]; then
		rm -rf "$target_dir"
	fi
	
	mkdir -p "$target_dir"

	cd "$target_dir"
	
	git clone $git_url $target_dir
	if [[ 
	git checkout $tag
	
	cd - >/dev/null

	log "got from git"
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

