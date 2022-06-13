package org.fanjr.simplify.context;

enum ContextProcessorStatus {
	SUCCESS, 
	
	/**
	 * 需要将新对象放回父级
	 */
	NEED_PUT_PERENT
}
