package ca.bc.gov.health.qa.autotest.plr.web.pages.plr.facility;

public class FacilityDialog {

	private final String dialogName;
	private final String formName;
	private final String effectiveFromStr;
	private final String effectiveToStr;
	private final String submitButtonName;
	private final String endReasonName;
	private final String addButtonImgText;
	
	

	public FacilityDialog(String dialogName, String formName, String effectiveFromStr, String effectiveToStr,
			String submitButtonName, String endReasonName, String addButtonImgText) {
		super();
		this.dialogName = dialogName;
		this.formName = formName;
		this.effectiveFromStr = effectiveFromStr;
		this.effectiveToStr = effectiveToStr;
		this.submitButtonName = submitButtonName;
		this.endReasonName = endReasonName;
		this.addButtonImgText = addButtonImgText;
	}



	public String getDialogName() {
		return dialogName;
	}



	public String getFormName() {
		return formName;
	}



	public String getEffectiveFromStr() {
		return effectiveFromStr;
	}



	public String getEffectiveToStr() {
		return effectiveToStr;
	}



	public String getSubmitButtonName() {
		return submitButtonName;
	}



	public String getEndReasonName() {
		return endReasonName;
	}



	public String getAddButtonImgText() {
		return addButtonImgText;
	}

	
	
	
	
}
