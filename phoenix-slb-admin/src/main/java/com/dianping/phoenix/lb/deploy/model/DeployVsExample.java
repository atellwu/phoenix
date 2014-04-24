package com.dianping.phoenix.lb.deploy.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class DeployVsExample {
    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    protected String orderByClause;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    protected boolean distinct;

    /**
     * This field was generated by MyBatis Generator.
     * This field corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    protected List<Criteria> oredCriteria;

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public DeployVsExample() {
        oredCriteria = new ArrayList<Criteria>();
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public String getOrderByClause() {
        return orderByClause;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public void setDistinct(boolean distinct) {
        this.distinct = distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public boolean isDistinct() {
        return distinct;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public List<Criteria> getOredCriteria() {
        return oredCriteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public void or(Criteria criteria) {
        oredCriteria.add(criteria);
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public Criteria or() {
        Criteria criteria = createCriteriaInternal();
        oredCriteria.add(criteria);
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public Criteria createCriteria() {
        Criteria criteria = createCriteriaInternal();
        if (oredCriteria.size() == 0) {
            oredCriteria.add(criteria);
        }
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    protected Criteria createCriteriaInternal() {
        Criteria criteria = new Criteria();
        return criteria;
    }

    /**
     * This method was generated by MyBatis Generator.
     * This method corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public void clear() {
        oredCriteria.clear();
        orderByClause = null;
        distinct = false;
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    protected abstract static class GeneratedCriteria {
        protected List<Criterion> criteria;

        protected GeneratedCriteria() {
            super();
            criteria = new ArrayList<Criterion>();
        }

        public boolean isValid() {
            return criteria.size() > 0;
        }

        public List<Criterion> getAllCriteria() {
            return criteria;
        }

        public List<Criterion> getCriteria() {
            return criteria;
        }

        protected void addCriterion(String condition) {
            if (condition == null) {
                throw new RuntimeException("Value for condition cannot be null");
            }
            criteria.add(new Criterion(condition));
        }

        protected void addCriterion(String condition, Object value, String property) {
            if (value == null) {
                throw new RuntimeException("Value for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value));
        }

        protected void addCriterion(String condition, Object value1, Object value2, String property) {
            if (value1 == null || value2 == null) {
                throw new RuntimeException("Between values for " + property + " cannot be null");
            }
            criteria.add(new Criterion(condition, value1, value2));
        }

        public Criteria andIdIsNull() {
            addCriterion("id is null");
            return (Criteria) this;
        }

        public Criteria andIdIsNotNull() {
            addCriterion("id is not null");
            return (Criteria) this;
        }

        public Criteria andIdEqualTo(Long value) {
            addCriterion("id =", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotEqualTo(Long value) {
            addCriterion("id <>", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThan(Long value) {
            addCriterion("id >", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdGreaterThanOrEqualTo(Long value) {
            addCriterion("id >=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThan(Long value) {
            addCriterion("id <", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdLessThanOrEqualTo(Long value) {
            addCriterion("id <=", value, "id");
            return (Criteria) this;
        }

        public Criteria andIdIn(List<Long> values) {
            addCriterion("id in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotIn(List<Long> values) {
            addCriterion("id not in", values, "id");
            return (Criteria) this;
        }

        public Criteria andIdBetween(Long value1, Long value2) {
            addCriterion("id between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andIdNotBetween(Long value1, Long value2) {
            addCriterion("id not between", value1, value2, "id");
            return (Criteria) this;
        }

        public Criteria andVsNameIsNull() {
            addCriterion("vs_name is null");
            return (Criteria) this;
        }

        public Criteria andVsNameIsNotNull() {
            addCriterion("vs_name is not null");
            return (Criteria) this;
        }

        public Criteria andVsNameEqualTo(String value) {
            addCriterion("vs_name =", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameNotEqualTo(String value) {
            addCriterion("vs_name <>", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameGreaterThan(String value) {
            addCriterion("vs_name >", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameGreaterThanOrEqualTo(String value) {
            addCriterion("vs_name >=", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameLessThan(String value) {
            addCriterion("vs_name <", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameLessThanOrEqualTo(String value) {
            addCriterion("vs_name <=", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameLike(String value) {
            addCriterion("vs_name like", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameNotLike(String value) {
            addCriterion("vs_name not like", value, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameIn(List<String> values) {
            addCriterion("vs_name in", values, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameNotIn(List<String> values) {
            addCriterion("vs_name not in", values, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameBetween(String value1, String value2) {
            addCriterion("vs_name between", value1, value2, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsNameNotBetween(String value1, String value2) {
            addCriterion("vs_name not between", value1, value2, "vsName");
            return (Criteria) this;
        }

        public Criteria andVsTagIsNull() {
            addCriterion("vs_tag is null");
            return (Criteria) this;
        }

        public Criteria andVsTagIsNotNull() {
            addCriterion("vs_tag is not null");
            return (Criteria) this;
        }

        public Criteria andVsTagEqualTo(String value) {
            addCriterion("vs_tag =", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagNotEqualTo(String value) {
            addCriterion("vs_tag <>", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagGreaterThan(String value) {
            addCriterion("vs_tag >", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagGreaterThanOrEqualTo(String value) {
            addCriterion("vs_tag >=", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagLessThan(String value) {
            addCriterion("vs_tag <", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagLessThanOrEqualTo(String value) {
            addCriterion("vs_tag <=", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagLike(String value) {
            addCriterion("vs_tag like", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagNotLike(String value) {
            addCriterion("vs_tag not like", value, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagIn(List<String> values) {
            addCriterion("vs_tag in", values, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagNotIn(List<String> values) {
            addCriterion("vs_tag not in", values, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagBetween(String value1, String value2) {
            addCriterion("vs_tag between", value1, value2, "vsTag");
            return (Criteria) this;
        }

        public Criteria andVsTagNotBetween(String value1, String value2) {
            addCriterion("vs_tag not between", value1, value2, "vsTag");
            return (Criteria) this;
        }

        public Criteria andStatusIsNull() {
            addCriterion("status is null");
            return (Criteria) this;
        }

        public Criteria andStatusIsNotNull() {
            addCriterion("status is not null");
            return (Criteria) this;
        }

        public Criteria andStatusEqualTo(DeployVsStatus value) {
            addCriterion("status =", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotEqualTo(DeployVsStatus value) {
            addCriterion("status <>", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThan(DeployVsStatus value) {
            addCriterion("status >", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusGreaterThanOrEqualTo(DeployVsStatus value) {
            addCriterion("status >=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThan(DeployVsStatus value) {
            addCriterion("status <", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLessThanOrEqualTo(DeployVsStatus value) {
            addCriterion("status <=", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusLike(DeployVsStatus value) {
            addCriterion("status like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotLike(DeployVsStatus value) {
            addCriterion("status not like", value, "status");
            return (Criteria) this;
        }

        public Criteria andStatusIn(List<DeployVsStatus> values) {
            addCriterion("status in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotIn(List<DeployVsStatus> values) {
            addCriterion("status not in", values, "status");
            return (Criteria) this;
        }

        public Criteria andStatusBetween(DeployVsStatus value1, DeployVsStatus value2) {
            addCriterion("status between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andStatusNotBetween(DeployVsStatus value1, DeployVsStatus value2) {
            addCriterion("status not between", value1, value2, "status");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdIsNull() {
            addCriterion("deploy_task_id is null");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdIsNotNull() {
            addCriterion("deploy_task_id is not null");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdEqualTo(Long value) {
            addCriterion("deploy_task_id =", value, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdNotEqualTo(Long value) {
            addCriterion("deploy_task_id <>", value, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdGreaterThan(Long value) {
            addCriterion("deploy_task_id >", value, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdGreaterThanOrEqualTo(Long value) {
            addCriterion("deploy_task_id >=", value, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdLessThan(Long value) {
            addCriterion("deploy_task_id <", value, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdLessThanOrEqualTo(Long value) {
            addCriterion("deploy_task_id <=", value, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdIn(List<Long> values) {
            addCriterion("deploy_task_id in", values, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdNotIn(List<Long> values) {
            addCriterion("deploy_task_id not in", values, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdBetween(Long value1, Long value2) {
            addCriterion("deploy_task_id between", value1, value2, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andDeployTaskIdNotBetween(Long value1, Long value2) {
            addCriterion("deploy_task_id not between", value1, value2, "deployTaskId");
            return (Criteria) this;
        }

        public Criteria andBeginDateIsNull() {
            addCriterion("begin_date is null");
            return (Criteria) this;
        }

        public Criteria andBeginDateIsNotNull() {
            addCriterion("begin_date is not null");
            return (Criteria) this;
        }

        public Criteria andBeginDateEqualTo(Date value) {
            addCriterion("begin_date =", value, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateNotEqualTo(Date value) {
            addCriterion("begin_date <>", value, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateGreaterThan(Date value) {
            addCriterion("begin_date >", value, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateGreaterThanOrEqualTo(Date value) {
            addCriterion("begin_date >=", value, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateLessThan(Date value) {
            addCriterion("begin_date <", value, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateLessThanOrEqualTo(Date value) {
            addCriterion("begin_date <=", value, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateIn(List<Date> values) {
            addCriterion("begin_date in", values, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateNotIn(List<Date> values) {
            addCriterion("begin_date not in", values, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateBetween(Date value1, Date value2) {
            addCriterion("begin_date between", value1, value2, "beginDate");
            return (Criteria) this;
        }

        public Criteria andBeginDateNotBetween(Date value1, Date value2) {
            addCriterion("begin_date not between", value1, value2, "beginDate");
            return (Criteria) this;
        }

        public Criteria andEndDateIsNull() {
            addCriterion("end_date is null");
            return (Criteria) this;
        }

        public Criteria andEndDateIsNotNull() {
            addCriterion("end_date is not null");
            return (Criteria) this;
        }

        public Criteria andEndDateEqualTo(Date value) {
            addCriterion("end_date =", value, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateNotEqualTo(Date value) {
            addCriterion("end_date <>", value, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateGreaterThan(Date value) {
            addCriterion("end_date >", value, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateGreaterThanOrEqualTo(Date value) {
            addCriterion("end_date >=", value, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateLessThan(Date value) {
            addCriterion("end_date <", value, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateLessThanOrEqualTo(Date value) {
            addCriterion("end_date <=", value, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateIn(List<Date> values) {
            addCriterion("end_date in", values, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateNotIn(List<Date> values) {
            addCriterion("end_date not in", values, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateBetween(Date value1, Date value2) {
            addCriterion("end_date between", value1, value2, "endDate");
            return (Criteria) this;
        }

        public Criteria andEndDateNotBetween(Date value1, Date value2) {
            addCriterion("end_date not between", value1, value2, "endDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateIsNull() {
            addCriterion("creation_date is null");
            return (Criteria) this;
        }

        public Criteria andCreationDateIsNotNull() {
            addCriterion("creation_date is not null");
            return (Criteria) this;
        }

        public Criteria andCreationDateEqualTo(Date value) {
            addCriterion("creation_date =", value, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateNotEqualTo(Date value) {
            addCriterion("creation_date <>", value, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateGreaterThan(Date value) {
            addCriterion("creation_date >", value, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateGreaterThanOrEqualTo(Date value) {
            addCriterion("creation_date >=", value, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateLessThan(Date value) {
            addCriterion("creation_date <", value, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateLessThanOrEqualTo(Date value) {
            addCriterion("creation_date <=", value, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateIn(List<Date> values) {
            addCriterion("creation_date in", values, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateNotIn(List<Date> values) {
            addCriterion("creation_date not in", values, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateBetween(Date value1, Date value2) {
            addCriterion("creation_date between", value1, value2, "creationDate");
            return (Criteria) this;
        }

        public Criteria andCreationDateNotBetween(Date value1, Date value2) {
            addCriterion("creation_date not between", value1, value2, "creationDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateIsNull() {
            addCriterion("last_modified_date is null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateIsNotNull() {
            addCriterion("last_modified_date is not null");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateEqualTo(Date value) {
            addCriterion("last_modified_date =", value, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateNotEqualTo(Date value) {
            addCriterion("last_modified_date <>", value, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateGreaterThan(Date value) {
            addCriterion("last_modified_date >", value, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateGreaterThanOrEqualTo(Date value) {
            addCriterion("last_modified_date >=", value, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateLessThan(Date value) {
            addCriterion("last_modified_date <", value, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateLessThanOrEqualTo(Date value) {
            addCriterion("last_modified_date <=", value, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateIn(List<Date> values) {
            addCriterion("last_modified_date in", values, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateNotIn(List<Date> values) {
            addCriterion("last_modified_date not in", values, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateBetween(Date value1, Date value2) {
            addCriterion("last_modified_date between", value1, value2, "lastModifiedDate");
            return (Criteria) this;
        }

        public Criteria andLastModifiedDateNotBetween(Date value1, Date value2) {
            addCriterion("last_modified_date not between", value1, value2, "lastModifiedDate");
            return (Criteria) this;
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table deploy_vs
     *
     * @mbggenerated do_not_delete_during_merge
     */
    public static class Criteria extends GeneratedCriteria {

        protected Criteria() {
            super();
        }
    }

    /**
     * This class was generated by MyBatis Generator.
     * This class corresponds to the database table deploy_vs
     *
     * @mbggenerated
     */
    public static class Criterion {
        private String condition;

        private Object value;

        private Object secondValue;

        private boolean noValue;

        private boolean singleValue;

        private boolean betweenValue;

        private boolean listValue;

        private String typeHandler;

        public String getCondition() {
            return condition;
        }

        public Object getValue() {
            return value;
        }

        public Object getSecondValue() {
            return secondValue;
        }

        public boolean isNoValue() {
            return noValue;
        }

        public boolean isSingleValue() {
            return singleValue;
        }

        public boolean isBetweenValue() {
            return betweenValue;
        }

        public boolean isListValue() {
            return listValue;
        }

        public String getTypeHandler() {
            return typeHandler;
        }

        protected Criterion(String condition) {
            super();
            this.condition = condition;
            this.typeHandler = null;
            this.noValue = true;
        }

        protected Criterion(String condition, Object value, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.typeHandler = typeHandler;
            if (value instanceof List<?>) {
                this.listValue = true;
            } else {
                this.singleValue = true;
            }
        }

        protected Criterion(String condition, Object value) {
            this(condition, value, null);
        }

        protected Criterion(String condition, Object value, Object secondValue, String typeHandler) {
            super();
            this.condition = condition;
            this.value = value;
            this.secondValue = secondValue;
            this.typeHandler = typeHandler;
            this.betweenValue = true;
        }

        protected Criterion(String condition, Object value, Object secondValue) {
            this(condition, value, secondValue, null);
        }
    }
}