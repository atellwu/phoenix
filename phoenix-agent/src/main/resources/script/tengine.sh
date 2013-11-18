#!/bin/bash
set -e
set -u

#trap "echo 'INT signal received'" INT
#trap "echo 'TERM signal received'" TERM
cd `dirname $0`
source ./util.sh
source ./tengine_func.sh

log "PID is $$"
log "CMD is $0 $@"

eval "`parse_argument_and_set_variable tengine_config_doc_base config_file virtual_server_name version tengine_config_git_doc_base tengine_reload dynamic_refresh_url dynamic_refresh_post_data refresh_method env git_url git_host func`"

ensure_not_empty tengine_config_doc_base="$tengine_config_doc_base" config_file="$config_file" virtual_server_name="$virtual_server_name"
ensure_not_empty version="$version" tengine_config_git_doc_base="$tengine_config_git_doc_base" env="$env"
ensure_not_empty git_url="$git_url" git_host="$git_host" func="$func"
ensure_not_empty tengine_reload="$tengine_reload"

if [ $tengine_reload -eq 0  ];then
	ensure_not_empty dynamic_refresh_url="$dynamic_refresh_url" dynamic_refresh_post_data="$dynamic_refresh_post_data" refresh_method="$refresh_method"
fi

$func $@
