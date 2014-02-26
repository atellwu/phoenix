package com.dianping.phoenix.service;

public interface ServiceProvider<T> {
	public Class<?> getServiceType();

	public T getService();
}
