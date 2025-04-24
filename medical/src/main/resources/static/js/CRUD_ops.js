// Patient CRUD Operations
const createPatient = async (patientData) => {
  try {
    const result = await db.patients.insertOne({
      ...patientData,
      createdAt: new Date()
    });
    return result;
  } catch (error) {
    if (error.code === 11000) {
      throw new Error('Username or Patient ID already exists');
    }
    throw error;
  }
};

const getPatientByUsername = async (username) => {
  return await db.patients.findOne({ username });
};

const getPatientById = async (patientId) => {
  return await db.patients.findOne({ _id: new ObjectId(patientId) });
};

const updatePatientInfo = async (patientId, updateData) => {
  return await db.patients.updateOne(
    { _id: new ObjectId(patientId) },
    { $set: updateData }
  );
};

// 1. CREATE: Book a new appointment
async function bookAppointment(doctorId, patientId, dateTime, duration, appointmentType, reasonForVisit) {
  // Validate patient exists
  const patient = await getPatientById(patientId);
  if (!patient) {
    throw new Error('Invalid patient ID');
  }
    return db.appointments.insertOne({
      doctorId: ObjectId(doctorId),
      patientId: ObjectId(patientId),
      dateTime: new Date(dateTime),
      duration: duration,
      status: "scheduled",
      appointmentType: appointmentType,
      reasonForVisit: reasonForVisit,
      notes: ""
    });
  }
  
  // 2. READ: Find all appointments for a specific doctor
  function getDoctorAppointments(doctorId, startDate, endDate) {
    return db.appointments.find({
      doctorId: ObjectId(doctorId),
      dateTime: {
        $gte: new Date(startDate),
        $lte: new Date(endDate)
      }
    }).sort({ dateTime: 1 });
  }
  
  // 3. READ: Find all appointments for a specific patient
  function getPatientAppointments(patientId) {
    return db.appointments.find({
      patientId: ObjectId(patientId)
    }).sort({ dateTime: 1 });
  }
  
  // 4. READ: Find appointments by status
  function getAppointmentsByStatus(status) {
    return db.appointments.find({
      status: status
    }).sort({ dateTime: 1 });
  }
  
  // 5. UPDATE: Reschedule an appointment
  function rescheduleAppointment(appointmentId, newDateTime) {
    // First get the original appointment
    const originalAppointment = db.appointments.findOne({
      _id: ObjectId(appointmentId)
    });
    
    if (!originalAppointment) {
      return { success: false, message: "Appointment not found" };
    }
    
    // Create a new appointment based on the original
    const newAppointment = {
      doctorId: originalAppointment.doctorId,
      patientId: originalAppointment.patientId,
      dateTime: new Date(newDateTime),
      duration: originalAppointment.duration,
      status: "scheduled",
      appointmentType: originalAppointment.appointmentType,
      reasonForVisit: originalAppointment.reasonForVisit,
      notes: originalAppointment.notes,
      previousAppointmentId: originalAppointment._id
    };
    
    // Update the status of the original appointment
    db.appointments.updateOne(
      { _id: ObjectId(appointmentId) },
      { $set: { status: "rescheduled" } }
    );
    
    // Insert the new appointment
    return db.appointments.insertOne(newAppointment);
  }
  
  // 6. DELETE: Cancel an appointment
  function cancelAppointment(appointmentId, cancellationReason) {
    return db.appointments.updateOne(
      { _id: ObjectId(appointmentId) },
      { 
        $set: { 
          status: "cancelled",
          notes: cancellationReason 
        } 
      }
    );
  }
  
  // 7. READ: Check doctor availability for a specific time slot
  function checkDoctorAvailability(doctorId, proposedDateTime, duration) {
    const startTime = new Date(proposedDateTime);
    const endTime = new Date(new Date(proposedDateTime).getTime() + duration * 60000);
    
    // Get doctor's schedule for the day of the week
    const dayOfWeek = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][startTime.getDay()];
    const doctor = db.doctors.findOne(
      { _id: ObjectId(doctorId) },
      { availableSlots: 1, breakTimes: 1 }
    );
    
    if (!doctor) {
      return { available: false, reason: "Doctor not found" };
    }
    
    // Check if doctor works on this day
    const workSlot = doctor.availableSlots.find(slot => slot.dayOfWeek === dayOfWeek);
    if (!workSlot) {
      return { available: false, reason: "Doctor does not work on this day" };
    }
    
    // Convert time strings to Date objects for comparison
    const workStart = new Date(startTime);
    workStart.setHours(parseInt(workSlot.startTime.split(':')[0]), parseInt(workSlot.startTime.split(':')[1]), 0);
    
    const workEnd = new Date(startTime);
    workEnd.setHours(parseInt(workSlot.endTime.split(':')[0]), parseInt(workSlot.endTime.split(':')[1]), 0);
    
    // Check if the proposed time is within working hours
    if (startTime < workStart || endTime > workEnd) {
      return { available: false, reason: "Proposed time is outside working hours" };
    }
    
    // Check if the proposed time conflicts with break times
    const breakConflict = doctor.breakTimes.find(breakTime => {
      if (breakTime.dayOfWeek !== dayOfWeek) return false;
      
      const breakStart = new Date(startTime);
      breakStart.setHours(parseInt(breakTime.startTime.split(':')[0]), parseInt(breakTime.startTime.split(':')[1]), 0);
      
      const breakEnd = new Date(startTime);
      breakEnd.setHours(parseInt(breakTime.endTime.split(':')[0]), parseInt(breakTime.endTime.split(':')[1]), 0);
      
      return (startTime < breakEnd && endTime > breakStart);
    });
    
    if (breakConflict) {
      return { available: false, reason: "Proposed time conflicts with doctor's break time" };
    }
    
    // Check if the proposed time conflicts with existing appointments
    const appointmentConflict = db.appointments.findOne({
      doctorId: ObjectId(doctorId),
      status: { $in: ["scheduled", "rescheduled"] },
      $or: [
        // Appointment starts during proposed time
        {
          dateTime: { 
            $gte: startTime, 
            $lt: endTime 
          }
        },
        // Appointment ends during proposed time
        {
          $expr: {
            $and: [
              { $lt: ["$dateTime", endTime] },
              { $gte: [{ $add: ["$dateTime", { $multiply: ["$duration", 60000] }] }, startTime] }
            ]
          }
        }
      ]
    });
    
    if (appointmentConflict) {
      return { available: false, reason: "Time slot conflicts with an existing appointment" };
    }
    
    return { available: true };
  }
  
  // 8. READ: Get a doctor's upcoming appointments for today
  function getTodaysAppointments(doctorId) {
    const today = new Date();
    today.setHours(0, 0, 0, 0);
    
    const tomorrow = new Date(today);
    tomorrow.setDate(tomorrow.getDate() + 1);
    
    return db.appointments.find({
      doctorId: ObjectId(doctorId),
      dateTime: {
        $gte: today,
        $lt: tomorrow
      },
      status: { $in: ["scheduled", "rescheduled"] }
    }).sort({ dateTime: 1 });
  }
  
  // 9. UPDATE: Complete an appointment and add notes
  function completeAppointment(appointmentId, notes) {
    return db.appointments.updateOne(
      { _id: ObjectId(appointmentId) },
      { 
        $set: { 
          status: "completed",
          notes: notes 
        } 
      }
    );
  }
  
  // 10. READ: Find available appointment slots for a doctor on a specific day
  function findAvailableSlots(doctorId, date, appointmentDuration) {
    const requestedDate = new Date(date);
    requestedDate.setHours(0, 0, 0, 0);
    
    const nextDay = new Date(requestedDate);
    nextDay.setDate(nextDay.getDate() + 1);
    
    // Get doctor's schedule for the day of the week
    const dayOfWeek = ["Sunday", "Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday"][requestedDate.getDay()];
    
    const doctor = db.doctors.findOne(
      { _id: ObjectId(doctorId) },
      { availableSlots: 1, breakTimes: 1 }
    );
    
    if (!doctor) {
      return { success: false, message: "Doctor not found" };
    }
    
    // Check if doctor works on this day
    const workSlot = doctor.availableSlots.find(slot => slot.dayOfWeek === dayOfWeek);
    if (!workSlot) {
      return { success: false, message: "Doctor does not work on this day" };
    }
    
    // Get all scheduled appointments for that day
    const appointments = db.appointments.find({
      doctorId: ObjectId(doctorId),
      dateTime: {
        $gte: requestedDate,
        $lt: nextDay
      },
      status: { $in: ["scheduled", "rescheduled"] }
    }).sort({ dateTime: 1 }).toArray();
    
    // Calculate available time slots
    const availableSlots = [];
    const slotDuration = appointmentDuration || 30; // Default to 30 minutes if not specified
    
    // Convert work hours to minutes since midnight for easier calculation
    const workStartParts = workSlot.startTime.split(':');
    const workStartMinutes = parseInt(workStartParts[0]) * 60 + parseInt(workStartParts[1]);
    
    const workEndParts = workSlot.endTime.split(':');
    const workEndMinutes = parseInt(workEndParts[0]) * 60 + parseInt(workEndParts[1]);
    
    // Get break times for this day
    const breakTimes = doctor.breakTimes
      .filter(breakTime => breakTime.dayOfWeek === dayOfWeek)
      .map(breakTime => {
        const startParts = breakTime.startTime.split(':');
        const endParts = breakTime.endTime.split(':');
        return {
          start: parseInt(startParts[0]) * 60 + parseInt(startParts[1]),
          end: parseInt(endParts[0]) * 60 + parseInt(endParts[1])
        };
      });
    
    // Convert appointments to minutes format
    const bookedSlots = appointments.map(apt => {
      const aptDate = new Date(apt.dateTime);
      const startMinutes = aptDate.getHours() * 60 + aptDate.getMinutes();
      return {
        start: startMinutes,
        end: startMinutes + apt.duration
      };
    });
    
    // Combine break times and booked slots into unavailable periods
    const unavailablePeriods = [...breakTimes, ...bookedSlots].sort((a, b) => a.start - b.start);
    
    // Start from work start time
    let currentMinute = workStartMinutes;
    
    while (currentMinute + slotDuration <= workEndMinutes) {
      // Check if this time slot overlaps with any unavailable period
      const isUnavailable = unavailablePeriods.some(period => 
        (currentMinute < period.end && currentMinute + slotDuration > period.start)
      );
      
      if (!isUnavailable) {
        // Format the time as HH:MM
        const hours = Math.floor(currentMinute / 60);
        const minutes = currentMinute % 60;
        
        availableSlots.push(
          `${hours.toString().padStart(2, '0')}:${minutes.toString().padStart(2, '0')}`
        );
      }
      
      // Move to next slot (typically in 15 or 30 minute increments)
      currentMinute += 15; // Using 15-minute increments here
    }
    
    return { 
      success: true, 
      date: requestedDate.toISOString().split('T')[0],
      availableSlots: availableSlots
    };
  }