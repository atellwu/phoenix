package com.dianping.phoenix.agent.page.nginx;

import org.unidal.web.mvc.ViewModel;

import com.dianping.phoenix.agent.AgentPage;
import com.dianping.phoenix.agent.response.entity.Response;
import com.dianping.phoenix.agent.response.transform.DefaultJsonBuilder;

public class Model extends ViewModel<AgentPage, Action, Context> {

    private Response response;

    public Model(Context ctx) {
        super(ctx);
    }

    @Override
    public Action getDefaultAction() {
        return Action.VIEW;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public String getResponseInJson() {
        return new DefaultJsonBuilder().build(response);
    }
}
