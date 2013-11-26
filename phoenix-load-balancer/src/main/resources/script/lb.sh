#!/bin/bash
set -e
set -u

#trap "echo 'INT signal received'" INT
#trap "echo 'TERM signal received'" TERM
cd `dirname $0`
source ./util.sh
source ./git_func.sh

log "PID is $$"
log "CMD is $0 $@"

eval "`parse_argument_and_set_variable git_url git_host tag comment target_dir func`"

ensure_not_empty git_url="$git_url" func="$func" git_host="$git_host" target_dir="$target_dir"

if [ $tengine_reload -eq 0  ];then
	ensure_not_empty dynamic_refresh_url="$dynamic_refresh_url" dynamic_refresh_post_data="$dynamic_refresh_post_data" refresh_method="$refresh_method"
fi

$func $@
