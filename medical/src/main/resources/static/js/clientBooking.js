document.addEventListener("DOMContentLoaded", function () {
  // Get CSRF token if using Spring Security
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector(
    'meta[name="_csrf_header"]'
  )?.content;

  // Initialize calendar
  const calendarEl = document.getElementById("calendar");
  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    height: 500,
    events: "/api/appointments/calendar",
    // ... rest of your calendar config
  });
  calendar.render();

  // Current user - you might get this from a session or JWT
  const currentPatientId = getCurrentPatientId(); // Implement this function

  // Fetch doctors
  async function fetchDoctors() {
    try {
      const headers = {
        "Content-Type": "application/json",
      };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      const response = await fetch("/api/doctors", {
        method: "GET",
        headers: headers,
      });

      if (!response.ok) throw new Error("Failed to fetch doctors");

      const doctors = await response.json();
      const doctorSelect = document.getElementById("doctorSelect");
      doctorSelect.innerHTML =
        '<option value="" disabled selected>Select a doctor</option>';

      doctors.forEach((doc) => {
        const option = document.createElement("option");
        option.value = doc.id;
        option.textContent = `${doc.firstName} ${doc.lastName} (${doc.specialization})`;
        doctorSelect.appendChild(option);
      });
    } catch (error) {
      console.error("Error:", error);
      showError("Error loading doctors. Please try again.");
    }
  }

  // Form submission
  document
    .getElementById("bookingForm")
    .addEventListener("submit", async (e) => {
      e.preventDefault();

      const formData = {
        doctorId: document.getElementById("doctorSelect").value,
        patientId: currentPatientId,
        date: document.getElementById("appointmentDate").value,
        time: document.getElementById("appointmentTime").value,
        type: document.getElementById("appointmentType").value,
        reason: document.getElementById("reasonForVisit").value,
      };

      try {
        const headers = {
          "Content-Type": "application/json",
        };
        if (csrfToken && csrfHeader) {
          headers[csrfHeader] = csrfToken;
        }

        const response = await fetch("/api/appointments", {
          method: "POST",
          headers: headers,
          body: JSON.stringify(formData),
        });

        if (!response.ok) {
          const errorData = await response.json();
          throw new Error(errorData.message || "Booking failed");
        }

        showSuccess("Appointment booked successfully!");
        document.getElementById("bookingForm").reset();
        calendar.refetchEvents();
      } catch (error) {
        console.error("Booking error:", error);
        showError(error.message);
      }
    });

  // Initialize
  fetchDoctors();

  function showSuccess(message) {
    const msgEl = document.getElementById("bookingMessage");
    msgEl.textContent = message;
    msgEl.className = "mt-3 text-success";
  }

  function showError(message) {
    const msgEl = document.getElementById("bookingMessage");
    msgEl.textContent = message;
    msgEl.className = "mt-3 text-danger";
  }
});

document.addEventListener("DOMContentLoaded", function () {
  let calendar;
  let selectedDate = null;
  const appointmentDateInput = document.getElementById("appointmentDate");
  const appointmentTimeSelect = document.getElementById("appointmentTime");
  const doctorSelect = document.getElementById("doctorSelect");
  const bookingForm = document.getElementById("bookingForm");
  const bookingMessage = document.getElementById("bookingMessage");

  // Initialize FullCalendar
  calendar = new FullCalendar.Calendar(document.getElementById("calendar"), {
    initialView: "dayGridMonth",
    headerToolbar: {
      left: "prev,next today",
      center: "title",
      right: "dayGridMonth",
    },
    selectable: true,
    select: function (info) {
      const today = new Date();
      today.setHours(0, 0, 0, 0);

      const selectedDay = new Date(info.startStr);
      if (selectedDay < today) {
        displayMessage("Cannot select past dates", "error");
        return;
      }

      selectedDate = info.startStr;
      appointmentDateInput.value = selectedDate;

      // Clear previous time slots
      appointmentTimeSelect.innerHTML =
        '<option value="" disabled selected>Select a time slot</option>';

      // If a doctor is selected, fetch available slots
      if (doctorSelect.value) {
        fetchAvailableTimeSlots(doctorSelect.value, selectedDate);
      }
    },
    eventClick: function (info) {
      // Handle click on existing appointments if needed
    },
    eventColor: "#378006",
    nowIndicator: true,
  });

  calendar.render();

  // Event listener for doctor selection
  doctorSelect.addEventListener("change", function () {
    if (selectedDate) {
      fetchAvailableTimeSlots(this.value, selectedDate);
    }
  });

  // Fetch available time slots from the server
  function fetchAvailableTimeSlots(doctorId, date) {
    fetch(`/api/availability?doctorId=${doctorId}&date=${date}`)
      .then((response) => {
        if (!response.ok) {
          throw new Error("Network response was not ok");
        }
        return response.json();
      })
      .then((availableSlots) => {
        populateTimeSlots(availableSlots);
      })
      .catch((error) => {
        console.error("Error fetching time slots:", error);
        displayMessage("Error fetching available time slots", "error");
      });
  }

  // Populate time slots dropdown
  function populateTimeSlots(availableSlots) {
    appointmentTimeSelect.innerHTML =
      '<option value="" disabled selected>Select a time slot</option>';

    if (availableSlots.length === 0) {
      const noSlotsOption = document.createElement("option");
      noSlotsOption.disabled = true;
      noSlotsOption.textContent = "No available slots for this date";
      appointmentTimeSelect.appendChild(noSlotsOption);
      return;
    }

    availableSlots.forEach((slot) => {
      const option = document.createElement("option");
      option.value = slot.startTime;
      option.textContent = `${slot.startTime} - ${slot.endTime}`;
      appointmentTimeSelect.appendChild(option);
    });
  }

  // Handle form submission
  bookingForm.addEventListener("submit", function (event) {
    event.preventDefault();

    const appointmentData = {
      doctorId: doctorSelect.value,
      appointmentDate: appointmentDateInput.value,
      appointmentTime: appointmentTimeSelect.value,
      appointmentType: document.getElementById("appointmentType").value,
      reasonForVisit: document.getElementById("reasonForVisit").value,
    };

    bookAppointment(appointmentData);
  });

  // Send booking request to server
  function bookAppointment(appointmentData) {
    fetch("/api/appointments/book", {
      method: "POST",
      headers: {
        "Content-Type": "application/json",
      },
      body: JSON.stringify(appointmentData),
    })
      .then((response) => {
        if (!response.ok) {
          return response.json().then((data) => {
            throw new Error(data.message || "Failed to book appointment");
          });
        }
        return response.json();
      })
      .then((data) => {
        displayMessage("Appointment booked successfully!", "success");
        bookingForm.reset();
        selectedDate = null;

        // Add the new appointment to the calendar
        calendar.addEvent({
          title: "Booked",
          start: `${appointmentData.appointmentDate}T${appointmentData.appointmentTime}`,
          allDay: false,
        });

        // Refresh calendar
        calendar.refetchEvents();
      })
      .catch((error) => {
        console.error("Error booking appointment:", error);
        displayMessage(
          error.message || "Error booking appointment. Please try again.",
          "error"
        );
      });
  }

  // Display messages to the user
  function displayMessage(message, type) {
    bookingMessage.innerHTML = `<div class="alert alert-${
      type === "error" ? "danger" : "success"
    }">${message}</div>`;
    setTimeout(() => {
      bookingMessage.innerHTML = "";
    }, 5000);
  }
});
