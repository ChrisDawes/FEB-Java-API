package com.hcl.feb.api;

import java.util.ArrayList;

public class FEBAttachmentHandler {
	private ArrayList<FEBAttachmentHandle> attachments = null;
	
	public FEBAttachmentHandler() {
		attachments = new ArrayList<FEBAttachmentHandle>();
	}
	
	public void addAttachment(FEBAttachmentHandle h) {
		attachments.add(h);
	}
}
