package system;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import factory.AdministratorDBOFactory;
import factory.CompanyDBOFactory;
import factory.RoomDBOFactory;
import model.Company;
import model.RoomBooking;
import model.user.Administrator;
import model.user.Employee;
import system.scheduler.CreditRefillExceutor;
import system.scheduler.Scheduler;

public class MCRB extends CRBCore{
	
	// Adapter
	private final static MCRB instance = new MCRB();
	private ArrayList<Company> companyList;
	private ArrayList<Administrator> admins;
	private Scheduler creditRefillSch;
	
	private MCRB() {
		super(RoomDBOFactory.create());
		companyList = CompanyDBOFactory.create();
		admins = AdministratorDBOFactory.create();
		// Weekly and monday 00:00
		setCreditRefillScheduler(7*24*60*60*1000, 1, LocalTime.of(0, 0, 0));
	}
	
	private void setCreditRefillScheduler(long period, int dayOfWeek,LocalTime time) {
		creditRefillSch = new CreditRefillExceutor("Credit Refill");
		
		//  delay start
		long delayStart = Scheduler.getDelay(dayOfWeek, time);
		
		ScheduledExecutorService service = Executors.newScheduledThreadPool(10);
		
		service.scheduleAtFixedRate(creditRefillSch, delayStart, period,
				TimeUnit.MILLISECONDS);
		
	}
	 
	
	public static MCRB getInstance() {
		return instance;
	}
	
	public void addCompany(Company company) {
		companyList.add(company);
		Collections.sort(companyList);
	}
	
	public ArrayList<Employee> getEmployeeList(String companyName) {
		int index = Collections.binarySearch(companyList, new Company(companyName));
		return (index < 0)? null : companyList.get(index).getEmployees();
	}
	
	public Employee login(Employee emp) {
		
		for(Company cmp: companyList) {
			ArrayList<Employee> cmpEMPList = cmp.getEmployees();
			Collections.sort(cmpEMPList);
			int index = Collections.binarySearch(cmpEMPList, emp);
			if(index > -1) 
				return (this.verifyPassword(cmpEMPList.get(index), emp))?
						cmpEMPList.get(index): null;
		}
		
		int index = Collections.binarySearch(admins, new Administrator(emp.getUsername(), emp.getPassword()));
		
		return (index > -1)? admins.get(index): null;
	}
	
	public boolean isAdmin(Employee emp) {
		Collections.sort(admins);
		int index = Collections.binarySearch(admins, emp);
		return (index >= 0)? true: false;
	}
	
	public Company getCompany(String companyName) {
		int index = Collections.binarySearch(companyList, new Company(companyName));
		return (index < 0)? null: companyList.get(index);
	}
	
	public Company getCompany(Employee emp) {
		Company result = null;
		for(Company cmp: companyList) {
			int index = Collections.binarySearch(cmp.getEmployees(), emp);
			if(index > -1) {
				result = cmp;
				break;
			}
		}
		return result;
	}
	
	public ArrayList<Company> searchCompany(String companyName) {
	 	return new ArrayList<Company>(companyList.stream()
				.filter( s -> s.getName().contains(companyName))
				.collect(Collectors.toList()));
	}
	
	public Company searchCompany(Employee emp) {
		for(Company cmp: companyList) {
			int index = Collections.binarySearch(cmp.getEmployees(), emp);
			if(index > -1) 
				return cmp;
		}
		return null;
	}
	
	public ArrayList<RoomBooking> getRoomBookingByMonthAndCompany(LocalDate monthDate, Company comp){
		ArrayList<RoomBooking> rbs = RoomBooking.getRoomBookingByMonth(bookingList, monthDate);
		ArrayList<RoomBooking> result = new ArrayList<RoomBooking>();
		rbs.forEach(rb -> {
			if(getCompany(rb.getEmployee())== comp)
				result.add(rb);
		});
		return result;
	}
	
	public Employee searchEmployee(String username) {
		for (Company cmp: companyList) {
			Employee result = Employee.search(cmp.getEmployees(), username);
			if(result != null) {
				return result;
			}			
		}
		return null;
	}
	
	
	public void removeEmployee(Employee emp) {
		for(Company cmp: companyList) {
			if(Collections.binarySearch(cmp.getEmployees(), emp) > -1)
				cmp.removeEmployee(emp);
		}
	}
	
	public ArrayList<RoomBooking> searchCompanyRoomBookig(LocalDate date, Employee emp){
		return RoomBooking.getRoomBookingByCompany(this.bookingList, searchCompany(emp).getEmployees(), date);
	}
	
	
	public void creditRefillAll(double creditMinute) {
		companyList.forEach(e -> e.getCredit().setMinute(creditMinute));
	}
	
}
