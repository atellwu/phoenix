/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Oct 28, 2013
 * 
 */
package com.dianping.phoenix.lb.constant;

/**
 * @author Leo Liang
 * 
 */
public enum MessageID {
    POOL_SAVE_FAIL("pool_save_fail"), //
    POOL_NAME_EMPTY("pool_name_empty"), //

    STRATEGY_SAVE_FAIL("strategy_save_fail"), //
    STRATEGY_NAME_EMPTY("strategy_name_empty"), //
    STRATEGY_TYPE_EMPTY("strategy_type_empty"), //
    //
    VIRTUALSERVER_ALREADY_EXISTS("vs_already_exists"), //
    VIRTUALSERVER_SAVE_FAIL("vs_save_fail"), //
    VIRTUALSERVER_CONCURRENT_MOD("vs_concurrent_mod"), //
    VIRTUALSERVER_NOT_EXISTS("vs_not_exists"), //
    VIRTUALSERVER_DEL_FAIL("vs_del_fail"), //
    VIRTUALSERVER_NAME_EMPTY("vs_name_empty"), //
    VIRTUALSERVER_NO_DEFAULT_POOL_NAME("vs_no_default_pool_name"), //
    VIRTUALSERVER_PORT_EMPTY("vs_port_empty"), //
    VIRTUALSERVER_TAGID_EMPTY("vs_pushid_empty"), //
    VIRTUALSERVER_DEFAULTPOOL_NOT_EXISTS("vs_defaultpool_not_exists"), //
    VIRTUALSERVER_DIRECTIVE_TYPE_NOT_SUPPORT("vs_directive_type_not_support"), //
    VIRTUALSERVER_LOCATION_NO_DIRECTIVE("vs_location_no_directive"), //
    VIRTUALSERVER_LOCATION_NO_PATTERN("vs_location_no_pattern"), //
    VIRTUALSERVER_LOCATION_NO_MATCHTYPE("vs_location_no_matchtype"), //
    VIRTUALSERVER_TAG_FAIL("vs_tag_fail"), //
    VIRTUALSERVER_TAG_LOAD_FAIL("vs_tag_load_fail"), //
    VIRTUALSERVER_TAG_NOT_FOUND("vs_tag_not_found"), //
    VIRTUALSERVER_TAG_LIST_FAIL("vs_tag_list_fail"), //

    POOL_STRATEGY_NOT_SUPPORT("pool_strategy_not_support"), //
    POOL_LOWER_THAN_MINAVAIL_PCT("pool_lower_than_minavail_pct"), //
    POOL_NO_MEMBER("pool_no_member"), //
    POOL_MEMBER_NO_NAME("pool_member_no_name"), //
    POOL_MEMBER_NO_IP("pool_member_no_ip"), //

    PROXY_PASS_NO_POOL("proxy_pass_no_pool"), //

    TAG_REMOVE_NOT_FOUND("tag_remove_not_found"), //

    DEPLOY_FIND_ACTIVE_DEPLOY_FAIL("deploy_find_active_deploy_fail"), //
    DEPLOY_ALREADY_RUNNING("deploy_already_running"), //
    DEPLOY_EXCEPTION("deploy_exception"),//
    
    GIT_EXCEPTION("git_exception"),//
    
    NGINX_CHECK_EXCEPTION("nginx_check_exception"),//

    ;

    private String messageId;

    private MessageID(String messageId) {
        this.messageId = messageId;
    }

    public String messageId() {
        return this.messageId;
    }

}
