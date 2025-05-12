document.addEventListener("DOMContentLoaded", function () {
  // Get CSRF token if using Spring Security
  const csrfToken = document.querySelector('meta[name="_csrf"]')?.content;
  const csrfHeader = document.querySelector(
    'meta[name="_csrf_header"]'
  )?.content;

  // Initialize calendar
  const calendarEl = document.getElementById("calendar");
  let selectedDateCell = null;

  const calendar = new FullCalendar.Calendar(calendarEl, {
    initialView: "dayGridMonth",
    height: 500,
    selectable: true,
    events: "/api/appointments/calendar",
    dateClick: function(info) {
      console.log("Date clicked:", info.dateStr);
      // Update the appointmentDate input with the clicked date in YYYY-MM-DD format
      const appointmentDateInput = document.getElementById("appointmentDate");
      appointmentDateInput.value = info.dateStr;
      // Manually trigger change event to update doctors list
      appointmentDateInput.dispatchEvent(new Event('change'));

      // Remove highlight from previously selected date cell
      if (selectedDateCell) {
        selectedDateCell.classList.remove("selected-date");
      }
      // Find the clicked date cell element by data-date attribute
      const newSelectedCell = calendarEl.querySelector(
        `[data-date="${info.dateStr}"]`
      );
      if (newSelectedCell) {
        newSelectedCell.classList.add("selected-date");
        selectedDateCell = newSelectedCell;
      }
    },
    // ... rest of your calendar config
  });
  calendar.render();

  // Current user - you might get this from a session or JWT
  function getCurrentPatientId() {
    // TODO: Replace with actual logic to get current patient ID
    return 1;
  }
  const currentPatientId = getCurrentPatientId();

  // Fetch doctors
  async function fetchDoctors(date) {
    try {
      const headers = {
        "Content-Type": "application/json",
      };
      if (csrfToken && csrfHeader) {
        headers[csrfHeader] = csrfToken;
      }

      let url = "/api/doctors";
      if (date) {
        url += `?date=${date}`;
      }

      const response = await fetch(url, {
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
        option.textContent = `${doc.firstName} ${doc.lastName}`;
        doctorSelect.appendChild(option);
      });
    } catch (error) {
      console.error("Error:", error);
      showError("Error loading doctors. Please try again.");
    }
  }

  // Listen for changes on appointmentDate input to fetch doctors for selected date
  document.getElementById("appointmentDate").addEventListener("change", (e) => {
    const selectedDate = e.target.value;
    console.log("Date input changed:", selectedDate);
    if (selectedDate) {
      fetchDoctors(selectedDate);
    } else {
      fetchDoctors();
    }
  });

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
