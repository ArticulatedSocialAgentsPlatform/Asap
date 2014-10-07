package asap.bmlflowvisualizer.utils;

public enum BMLBlockStatus {

	NONE,  			/* The BML realizer has no knowledge of this BML behavior 
					block used before it is sent to the BML realizer */
	SUBMITTED, 	/* The BML behavior block is sent to the BML
					realizer, and is currently being scheduled*/
	IN_PREP, 		/* The BML realizer is currently scheduling the behavior */
	PENDING, 		/* The BML realizer has scheduled the BML behavior
					block and is waiting to be activated */
	LURKING, 		/* The BML behavior block is waiting to be executed */
	IN_EXEC, 		/* The BML behavior block is currently being
					executed by the realizer */
	SUBSIDING, 	/* The BML behavior block is currently subsiding, that is all 
					behaviors are currently either done or in their relax phase (blockProgress, relax) */	
	DONE, 			/* The BML behavior block has been executed */
	INTERRUPT_REQUESTED, /* A request to interrupt the BML behavior block
							has been sent to the realizer, further status unknown */
	REVOKED, 		/* The BML behavior block is revoked before
					executed has started. BML was typically in status PENDING or LURKING,
					possibly also NONE, SUBMITTED, IN_PREP. */
	INTERRUPTED, 	/* The BML behavior block is interrupted during
					execution. BML was in status IN_EXEC. */
	FAILED;			/* The BML realizer could not realize the BML
					behavior block */
}
