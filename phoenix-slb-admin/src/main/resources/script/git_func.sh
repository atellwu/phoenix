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
		cp -r git/.ssh/id_rsa ~/.ssh/phoenixlb
		cp -r git/.ssh/id_rsa.pub ~/.ssh/phoenixlb.pub
		chmod 600 ~/.ssh/phoenixlb
		chmod 600 ~/.ssh/phoenixlb.pub
		cat <<-END >> $ssh_config
			
			Host $git_host
			IdentityFile ~/.ssh/phoenixlb
			StrictHostKeyChecking no
		END
		chmod 600 $ssh_config
		log "phoenix-load-balancer private key added to .ssh/config"
	fi
}

function commit_all_changes {
	cd $target_dir
	local change_files=`git status --short | wc -l`
	if [ $change_files -gt 0 ];then
		log "git committing $change_files files"
		git add -A
		git commit -m "$comment"
	else
		log "no file changed, no commit necessary"
	fi
	cd - >/dev/null
}

function tag_and_push {
	add_ssh_private_key
	cd $target_dir
	local change_files=`git status --short | wc -l`
	if [ $change_files -gt 0 ];then
		log_error "There's unstaged changes in $target_dir, exit code is $?"; 
		exit 1; 
	else
		log "adding git tag $tag for folder $target_dir"
		git tag -a $tag -m"$comment"
		git push
		git push --tags
	fi
	cd - >/dev/null
}

function push {
	add_ssh_private_key
	cd $target_dir
	local change_files=`git status --short | wc -l`
	if [ $change_files -gt 0 ];then
		log_error "There's unstaged changes in $target_dir, exit code is $?"; 
		exit 1; 
	else
		log "git pushing for folder $target_dir"
		git push
	fi
	cd - >/dev/null
}

function clone {
	add_ssh_private_key

	if [[  -d "$target_dir" ]]; then
		rm -rf "$target_dir"
		log "$target_dir cleared"
	fi
	
	mkdir -p "$target_dir"
	log "$target_dir created"

	cd "$target_dir"
	
	log "cloning $git_url to $target_dir"
	git clone $git_url $target_dir
	if [ "$tag" ]; then
		log "checking out to tag $tag"
		git checkout $tag
	fi

	cd - >/dev/null
}

function rollback {
	log "rolling back $target_dir"
	rm -rf $target_dir/*
	
	cd $target_dir/
	git reset --hard
	cd - >/dev/null
}