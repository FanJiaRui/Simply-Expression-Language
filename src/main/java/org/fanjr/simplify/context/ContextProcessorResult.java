package org.fanjr.simplify.context;

class ContextProcessorResult {
	private final static ThreadLocal<ContextProcessorResult> POOL = ThreadLocal
			.withInitial(ContextProcessorResult::new);

	/**
	 * 处理状态
	 */
	ContextProcessorStatus status;

	/**
	 * 处理结果
	 */
	Object value;

	private ContextProcessorResult() {
		// skip
	}

	static ContextProcessorResult newResult(ContextProcessorStatus status, Object value) {
		ContextProcessorResult result = POOL.get();
		result.status = status;
		result.value = value;
		return result;
	}

	static ContextProcessorResult newSuccessResult(Object value) {
		ContextProcessorResult result = POOL.get();
		result.status = ContextProcessorStatus.SUCCESS;
		result.value = value;
		return result;
	}

	static ContextProcessorResult newNeedPutPerent(Object value) {
		ContextProcessorResult result = POOL.get();
		result.status = ContextProcessorStatus.NEED_PUT_PERENT;
		result.value = value;
		return result;
	}

}
