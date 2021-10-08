package com.clinic.dental.model.appointment.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.validation.Valid;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.clinic.dental.exceptions.CustomMessageException;
import com.clinic.dental.exceptions.DataIdNotFoundException;
import com.clinic.dental.model.appointment.AppointmentEntity;
import com.clinic.dental.model.appointment.converter.AppointmentConverter;
import com.clinic.dental.model.appointment.dto.DisplayAppointmentDto;
import com.clinic.dental.model.appointment.dto.RezerveSlotDto;
import com.clinic.dental.model.appointment.dto.ChangeTimeAppointmentDto;
import com.clinic.dental.model.appointment.dto.CreatePublicAppointmentDto;
import com.clinic.dental.model.appointment.dto.SlotDto;
import com.clinic.dental.model.appointment.enums.Status;
import com.clinic.dental.model.appointment.repository.AppointmentRepository;
import com.clinic.dental.model.original_appointment.OriginalAppointmentEntity;
import com.clinic.dental.model.original_appointment.converter.OriginalAppointmentConverter;
import com.clinic.dental.model.original_appointment.repository.OriginalAppointmentRepository;
import com.clinic.dental.model.user.UserEntity;
import com.clinic.dental.model.user.enums.Role;
import com.clinic.dental.model.user.service.UserService;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AppointmentServiceImpl implements AppointmentService {

	private final AppointmentRepository appointmentRepo;
	private final OriginalAppointmentRepository originalAppointmentRepo;
	private final UserService userService;

	public static final Integer WORK_HOUR_START = 8;
	public static final Integer WORK_HOUR_END = 16;

	@Override
	public List<DisplayAppointmentDto> getAllAppointments() {
		List<DisplayAppointmentDto> appointments = new ArrayList<>();
		appointmentRepo.findAll().stream()
				.forEach(appointment -> appointments.add(AppointmentConverter.toDto(appointment)));
		return appointments;
	}

	@Override
	public DisplayAppointmentDto getAppointmentById(Long id) {
		UserEntity user = userService.getAuthenticatedUser();
		AppointmentEntity appointment = appointmentRepo.locateById(id);
		if(appointment != null) {
			if(user.getRole().equals(Role.ROLE_PUBLIC) && appointment.getPatient().equals(user)) {
				return AppointmentConverter.toDto(appointment);
			}else if(user.getRole().equals(Role.ROLE_DOCTOR) && appointment.getDentist().equals(user.getUsername())) {
				return AppointmentConverter.toDto(appointment);
			}else {
				return AppointmentConverter.toDto(appointment);
			}
		}
		throw new DataIdNotFoundException("Can not find appointment with id: "+id);
	}

	@Override
	@Transactional
	public CreatePublicAppointmentDto createAppointment(@Valid CreatePublicAppointmentDto appointmentDto) {
		return AppointmentConverter.toDisplayDto(appointmentRepo.save(AppointmentConverter.toEntity(appointmentDto)));
	}

	@Override
	@Transactional
	public CreatePublicAppointmentDto updateAppointmentById(@Valid CreatePublicAppointmentDto appointmentDto, Long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	@Transactional
	public Void deleteAppointmentById(Long id) {
		AppointmentEntity appointment = appointmentRepo.getById(id);
		if (appointment != null) {
			appointmentRepo.delete(appointment);
		}
		throw new DataIdNotFoundException("Can not find appointment wit given ID: " + id);
	}

	@Override
	public List<SlotDto> getFreeTimes() {
		List<LocalDateTime> timeList = getSevenDaysForward(LocalDateTime.now().truncatedTo(ChronoUnit.HOURS).plusHours(1),
				LocalDateTime.now().plusWeeks(1L));
		List<AppointmentEntity> appointmentList = appointmentRepo.findAllAfterToday().stream()
				.filter(app -> !app.getStatus().equals(Status.REFUZED)).toList();
		final List<String> doctorsList = userService.getDoctorsName(Role.ROLE_DOCTOR.toString());
		
		List<SlotDto> listOfSlots = new ArrayList<>();
		

		timeList.stream()
				.map(time -> listOfSlots.add(
						new SlotDto(time.toLocalDate(), time.toLocalTime(), time.plusHours(1).toLocalTime(), doctorsList)))
				.collect(Collectors.toList());
		
		
//		for(AppointmentEntity app: appointmentList) {
//			for(SlotDto slot: listOfSlots) {
//				if(slot.getDate().equals(app.getDate()) && slot.getVisitStart().equals(app.getStartTime())&& slot.getDoctors().size()>1) {
//					listOfSlots.get(listOfSlots.indexOf(slot)).getDoctors().remove(app.getDentist());
//				}else if(slot.getDate().equals(app.getDate()) && slot.getVisitStart().equals(app.getStartTime()) && slot.getDoctors().size() == 1) {
//					listOfSlots.remove(slot);
//				}else {
//					System.out.println(doctorsList);
//					slot.setDoctors(doctorsList);
//				}
//			}
//		}
		

//		for(AppointmentEntity app: appointmentList) {
//			listOfSlots.stream()
//				.filter(slot -> slot.getDate().equals(app.getDate()) && slot.getVisitStart().equals(app.getStartTime())&& slot.getDoctors().size()>1)
//				.map(slot -> slot.getDoctors().remove(app.getDentist()))
//				.collect(Collectors.toList());
//			return listOfSlots;
//		}
		
		appointmentList.forEach(app -> {
			listOfSlots.stream().filter(
					slot -> slot.getDate().equals(app.getDate()) && slot.getVisitStart().equals(app.getStartTime()))
					.findFirst().map(slot -> listOfSlots.remove(slot));
		});

//		appointmentList.forEach(app ->{
//			listOfSlots.stream()
//			.flatMap(slot -> slot.getDoctors().stream()).filter(doctor -> doctor.equals(app.getDentist()))
//			.findFirst()
//			.map(doc -> doc.replace(doc, ""));
//		});

		return listOfSlots;
	}

	public static List<LocalDateTime> getSevenDaysForward(LocalDateTime startDate, LocalDateTime endDate) {
		long numOfDaysBetween = ChronoUnit.HOURS.between(startDate, endDate);
		return IntStream.iterate(0, i -> i + 1).limit(numOfDaysBetween).mapToObj(i -> startDate.plusHours(i))
				.filter(i -> isWorkingHours(i)).filter(i -> isWorkingDay(i)).collect(Collectors.toList());
	}

	private static boolean isWorkingDay(final LocalDateTime time) {
		return time.getDayOfWeek().getValue() < DayOfWeek.SATURDAY.getValue();
	}

	private static boolean isWorkingHours(final LocalDateTime time) {
		int hour = time.getHour();
		return WORK_HOUR_START <= hour && hour <= WORK_HOUR_END;
	}

	@Override
	@Transactional
	public DisplayAppointmentDto rezerveAppointment(@Valid RezerveSlotDto rezerveDto) {
		UserEntity thisUser = userService.getAuthenticatedUser();
		List<SlotDto> freeSlots = getFreeTimes();
		boolean hasFreeSlot = hasSlot(freeSlots, rezerveDto.getDay(), rezerveDto.getStartTime());
		UserEntity doctorSelected = userService.getDoctorByUsername(rezerveDto.getDoctorUsername());

		if (hasFreeSlot) {
			AppointmentEntity appointment = AppointmentConverter.rezervationToEntity(rezerveDto, thisUser,
					doctorSelected.getUsername());
			return AppointmentConverter.toDto(appointmentRepo.save(appointment));
		}
		throw new CustomMessageException("Please pick another time!");
	}

	private boolean hasSlot(List<SlotDto> slots, LocalDate day, LocalTime startTime) {
		return slots.stream().anyMatch(slot -> slot.getVisitStart().equals(startTime.truncatedTo(ChronoUnit.HOURS))
				&& slot.getDate().equals(day));
	}

	@Override
	@Transactional
	public DisplayAppointmentDto approveAppointmentById(Long id) {
		AppointmentEntity appointment = appointmentRepo.getById(id);
		if (appointment != null) {
			if (appointment.getStatus().equals(Status.APPENDING)) {
				appointment.setStatus(Status.ACTIVE);
				return AppointmentConverter.toDto(appointmentRepo.save(appointment));
			}
			throw new CustomMessageException("Appointment is not on PENDING status!");
		}
		throw new DataIdNotFoundException("Can not locate rezervation with id: " + id);
	}

	@Override
	@Transactional
	public DisplayAppointmentDto changeAppointmentTimeById(Long id, @Valid ChangeTimeAppointmentDto dto) {
		AppointmentEntity appointment = appointmentRepo.getById(id);
		if (appointment != null) {
			boolean hasFreeSlot = hasSlot(getFreeTimes(), dto.getDay(), dto.getStartTime());
			if (hasFreeSlot) {
				OriginalAppointmentEntity originalDate = OriginalAppointmentConverter.toEntity(appointment);
				appointment.setOriginalDate(originalAppointmentRepo.save(originalDate));
				appointment.setStatus(Status.APPENDING_USER);
				appointment.setDate(dto.getDay());
				appointment.setStartTime(dto.getStartTime().truncatedTo(ChronoUnit.HOURS));
				appointment.setEndTime(dto.getStartTime().plusHours(1).truncatedTo(ChronoUnit.HOURS));
				return AppointmentConverter.toDto(appointmentRepo.save(appointment));
			}
			throw new CustomMessageException("Please pick another time!");
		}
		throw new DataIdNotFoundException("Can not find appointment with given id: " + id);
	}

	@Override
	@Transactional
	public DisplayAppointmentDto closeAppointmentById(Long id) {
		AppointmentEntity appointment = appointmentRepo.getById(id);
		if (appointment != null) {
			if (appointment.getDate().equals(LocalDate.now()) || appointment.getDate().isBefore(LocalDate.now())) {
				if (appointment.getEndTime().isBefore(LocalTime.now()) && appointment.getFeedback() != null) {
					appointment.setStatus(Status.DONE);
					return AppointmentConverter.toDto(appointmentRepo.save(appointment));
				}
				throw new CustomMessageException("Appointment not finished or feedback not given yet!");
			}
			throw new CustomMessageException("Appointment day is not reached yet!");
		}
		throw new DataIdNotFoundException("Can not find appointment with id: " + id);
	}

	@Override
	public List<DisplayAppointmentDto> getMyAllAppointments() {
		UserEntity user = userService.getAuthenticatedUser();
		if (user.getRole().equals(Role.ROLE_DOCTOR)) {
			List<DisplayAppointmentDto> appointments = new ArrayList<>();
			appointmentRepo.findByDentist(user.getUsername()).stream()
					.forEach(appointment -> appointments.add(AppointmentConverter.toDto(appointment)));
			return appointments;
		} else if (user.getRole().equals(Role.ROLE_PUBLIC)) {
			List<DisplayAppointmentDto> appointments = new ArrayList<>();
			appointmentRepo.findByPatient(user).stream()
					.forEach(appointment -> appointments.add(AppointmentConverter.toDto(appointment)));
			return appointments;
		} else {
			return getAllAppointments();
		}
	}

	@Override
	@Transactional
	public DisplayAppointmentDto approveNewTimeAppointment(Long id) {
		UserEntity user = userService.getAuthenticatedUser();
		AppointmentEntity appointment = appointmentRepo.findByIdAndStatus(id, Status.APPENDING_USER);
		if (appointment.getPatient().equals(user)) {
			appointment.setStatus(Status.ACTIVE);
			return AppointmentConverter.toDto(appointmentRepo.save(appointment));
		}
		throw new DataIdNotFoundException("Cannot locate your appointment by id:" + id);
	}

	@Override
	@Transactional
	public DisplayAppointmentDto refuzeNewTimeAppointment(Long id) {
		UserEntity user = userService.getAuthenticatedUser();
		AppointmentEntity appointment = appointmentRepo.findByIdAndStatus(id, Status.APPENDING_USER);
		if (appointment != null) {
			if (appointment.getPatient().equals(user)) {
				OriginalAppointmentEntity originalDate = appointment.getOriginalDate();
				appointment.setStatus(Status.USER_REFUZED_TIME);
				appointment.setDate(originalDate.getDay());
				appointment.setStartTime(originalDate.getStartTime());
				appointment.setEndTime(originalDate.getEndTime());
				appointment.setLastUpdatedAt(LocalDateTime.now());
				return AppointmentConverter.toDto(appointmentRepo.save(appointment));
			}
			throw new DataIdNotFoundException("Cannot locate your appointment by id:" + id);
		}
		throw new DataIdNotFoundException("Can not find with given id: " + id + ". please verify dates");
	}

	@Override
	@Transactional
	public DisplayAppointmentDto cancelAppointment(Long id) {
		UserEntity user = userService.getAuthenticatedUser();
		AppointmentEntity appointment = appointmentRepo.findAppointmentToCancel(id);
		if (appointment != null) {
			if (appointment.getPatient().equals(user)) {
				appointment.setStatus(Status.USER_CANCELLED);
				appointment.setLastUpdatedAt(LocalDateTime.now());
				return AppointmentConverter.toDto(appointmentRepo.save(appointment));
			} else if (appointment.getDentist().equals(user.getUsername())) {
				appointment.setStatus(Status.DOCTOR_CANCELLED);
				appointment.setLastUpdatedAt(LocalDateTime.now());
				return AppointmentConverter.toDto(appointmentRepo.save(appointment));
			}
			throw new CustomMessageException("Selected Appointment not your appointment!");
		}
		throw new DataIdNotFoundException("Can not find with given id: " + id + ".  Can not Cancel!");
	}

}