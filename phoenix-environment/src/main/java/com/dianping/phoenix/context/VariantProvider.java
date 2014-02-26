package com.dianping.phoenix.context;

public interface VariantProvider {
	public String[] getSupportedVariants();

	public String getVariant(String name);
}
