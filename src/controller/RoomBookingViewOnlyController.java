package controller;

import java.time.LocalDate;

import system.CRB;
import view.RoomBookingView;

public class RoomBookingViewOnlyController extends Controller {
	
	private RoomBookingView view;
	public RoomBookingViewOnlyController(RoomBookingView view) {
		this.view = view;
	}
	
	public void execute() {
		LocalDate date = view.getDate();
		if(date == null)
			return;
		
		CRB instance = CRB.getInstance();
		view.showDateRoomingDetails(date, instance.getRooms(), instance.getRoomBookingByDate(date));

		
	}

	@Override
	public String getDescription() {
		// TODO Auto-generated method stub
		return "View Room Avaiable";
	}

 	
}
