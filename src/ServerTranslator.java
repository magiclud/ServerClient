
public class ServerTranslator {
	String message = null;
	String playerID = null;
	
	
	ServerTranslator() {
	}
	
	String getMessage() {
		return message;
	}
	void spytajCzyChceDolaczycDoGry(String playerID) {
		CommunicationBox.orderForServer = "Chcesz graæ ?";
		this.playerID = playerID;
	}
	public static void startMethod(String playerID, String polecenie) {
		switch(polecenie){
			case "ADD": { CommunicationBox.orderForRozgrywka="ADD";
						CommunicationBox.whichPlayerRogrywka=playerID;
						};  
		}
	}
	public static void wyslijWiadomosc(String message, String playerID) {
		CommunicationBox.orderForServer = message;
		CommunicationBox.whichPlayerServer = playerID;
	}
}

