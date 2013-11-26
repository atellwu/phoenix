function nginx_check {
	sudo -u root /etc/init.d/nginx -t -c $config
}

