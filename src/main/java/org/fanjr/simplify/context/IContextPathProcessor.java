package org.fanjr.simplify.context;

interface IContextPathProcessor {

	Object doGetProcess(Object input);

	Object newContainer(Object value, boolean ordered);

	ContextProcessorResult doPutProcess(Object input, Object value, boolean ordered);

}
