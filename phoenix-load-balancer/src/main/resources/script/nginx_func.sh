function nginx_check {
	sudo /etc/init.d/nginx -t -c $config
}

