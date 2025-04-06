// Doctor Schema
db.createCollection("doctors", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["username", "legalName", "medicalId", "specialization", "biography"],
        properties: {
          username: {
            bsonType: "string",
            description: "Doctor's username for login - must be unique"
          },
          legalName: {
            bsonType: "object",
            required: ["firstName", "lastName"],
            properties: {
              firstName: {
                bsonType: "string",
                description: "Doctor's first name"
              },
              lastName: {
                bsonType: "string",
                description: "Doctor's last name"
              },
              middleName: {
                bsonType: "string",
                description: "Doctor's middle name (optional)"
              }
            }
          },
          medicalId: {
            bsonType: "string",
            description: "Doctor's unique medical identification number"
          },
          specialization: {
            bsonType: "string",
            description: "Doctor's area of specialization"
          },
          biography: {
            bsonType: "string",
            description: "Summary of doctor's professional background"
          },
          availableSlots: {
            bsonType: "array",
            description: "Doctor's available time slots",
            items: {
              bsonType: "object",
              required: ["dayOfWeek", "startTime", "endTime"],
              properties: {
                dayOfWeek: {
                  bsonType: "string",
                  enum: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
                  description: "Day of the week"
                },
                startTime: {
                  bsonType: "string",
                  description: "Start time in HH:MM format"
                },
                endTime: {
                  bsonType: "string",
                  description: "End time in HH:MM format"
                }
              }
            }
          },
          breakTimes: {
            bsonType: "array", 
            description: "Doctor's break times",
            items: {
              bsonType: "object",
              required: ["dayOfWeek", "startTime", "endTime"],
              properties: {
                dayOfWeek: {
                  bsonType: "string",
                  enum: ["Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"],
                  description: "Day of the week"
                },
                startTime: {
                  bsonType: "string",
                  description: "Break start time in HH:MM format"
                },
                endTime: {
                  bsonType: "string",
                  description: "Break end time in HH:MM format"
                }
              }
            }
          },
          contactInfo: {
            bsonType: "object",
            properties: {
              email: {
                bsonType: "string",
                description: "Doctor's email address"
              },
              phone: {
                bsonType: "string",
                description: "Doctor's contact number"
              }
            }
          }
        }
      }
    }
  });
  
  // Create an index on username and medicalId to ensure uniqueness
  db.doctors.createIndex({ "username": 1 }, { unique: true });
  db.doctors.createIndex({ "medicalId": 1 }, { unique: true });
  
  // Appointments Collection
  db.createCollection("appointments", {
    validator: {
      $jsonSchema: {
        bsonType: "object",
        required: ["doctorId", "patientId", "dateTime", "duration", "status"],
        properties: {
          doctorId: {
            bsonType: "objectId",
            description: "Reference to the doctor"
          },
          patientId: {
            bsonType: "objectId", 
            description: "Reference to the patient"
          },
          dateTime: {
            bsonType: "date",
            description: "Date and time of the appointment"
          },
          duration: {
            bsonType: "int",
            description: "Duration of appointment in minutes"
          },
          status: {
            bsonType: "string",
            enum: ["scheduled", "completed", "cancelled", "rescheduled"],
            description: "Current status of the appointment"
          },
          appointmentType: {
            bsonType: "string",
            description: "Type of appointment (e.g., check-up, follow-up, consultation)"
          },
          notes: {
            bsonType: "string",
            description: "Additional notes regarding the appointment"
          },
          reasonForVisit: {
            bsonType: "string",
            description: "Patient's reason for visit"
          },
          previousAppointmentId: {
            bsonType: "objectId",
            description: "Reference to previous appointment if this is a rescheduled appointment"
          }
        }
      }
    }
  });
  
  // Create indexes for efficient querying
  db.appointments.createIndex({ "doctorId": 1, "dateTime": 1 });
  db.appointments.createIndex({ "patientId": 1, "dateTime": 1 });
  db.appointments.createIndex({ "status": 1 });
  
  // Insert 5 sample doctors
  db.doctors.insertMany([
    {
      username: "dr.smith",
      legalName: {
        firstName: "John",
        lastName: "Smith"
      },
      medicalId: "MD12345",
      specialization: "Cardiology",
      biography: "Dr. Smith is a board-certified cardiologist with over 15 years of experience in treating various heart conditions.",
      availableSlots: [
        { dayOfWeek: "Monday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Wednesday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Friday", startTime: "09:00", endTime: "15:00" }
      ],
      breakTimes: [
        { dayOfWeek: "Monday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Wednesday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Friday", startTime: "12:00", endTime: "13:00" }
      ],
      contactInfo: {
        email: "john.smith@medicalmanager.com",
        phone: "555-123-4567"
      }
    },
    {
      username: "dr.patel",
      legalName: {
        firstName: "Riya",
        lastName: "Patel"
      },
      medicalId: "MD67890",
      specialization: "Pediatrics",
      biography: "Dr. Patel specializes in pediatric care with a focus on early childhood development and preventive healthcare.",
      availableSlots: [
        { dayOfWeek: "Tuesday", startTime: "08:00", endTime: "16:00" },
        { dayOfWeek: "Thursday", startTime: "08:00", endTime: "16:00" },
        { dayOfWeek: "Saturday", startTime: "10:00", endTime: "14:00" }
      ],
      breakTimes: [
        { dayOfWeek: "Tuesday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Thursday", startTime: "12:00", endTime: "13:00" }
      ],
      contactInfo: {
        email: "riya.patel@medicalmanager.com",
        phone: "555-789-0123"
      }
    },
    {
      username: "dr.johnson",
      legalName: {
        firstName: "Michael",
        lastName: "Johnson"
      },
      medicalId: "MD54321",
      specialization: "Orthopedics",
      biography: "Dr. Johnson is an orthopedic surgeon specializing in sports medicine and joint replacements with over 20 years of experience.",
      availableSlots: [
        { dayOfWeek: "Monday", startTime: "10:00", endTime: "18:00" },
        { dayOfWeek: "Thursday", startTime: "10:00", endTime: "18:00" },
        { dayOfWeek: "Friday", startTime: "10:00", endTime: "16:00" }
      ],
      breakTimes: [
        { dayOfWeek: "Monday", startTime: "13:00", endTime: "14:00" },
        { dayOfWeek: "Thursday", startTime: "13:00", endTime: "14:00" },
        { dayOfWeek: "Friday", startTime: "13:00", endTime: "14:00" }
      ],
      contactInfo: {
        email: "michael.johnson@medicalmanager.com",
        phone: "555-456-7890"
      }
    },
    {
      username: "dr.garcia",
      legalName: {
        firstName: "Elena",
        lastName: "Garcia"
      },
      medicalId: "MD98765",
      specialization: "Dermatology",
      biography: "Dr. Garcia is a dermatologist with expertise in skin cancer detection and cosmetic dermatology procedures.",
      availableSlots: [
        { dayOfWeek: "Tuesday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Wednesday", startTime: "09:00", endTime: "17:00" },
        { dayOfWeek: "Friday", startTime: "09:00", endTime: "15:00" }
      ],
      breakTimes: [
        { dayOfWeek: "Tuesday", startTime: "12:30", endTime: "13:30" },
        { dayOfWeek: "Wednesday", startTime: "12:30", endTime: "13:30" },
        { dayOfWeek: "Friday", startTime: "12:30", endTime: "13:30" }
      ],
      contactInfo: {
        email: "elena.garcia@medicalmanager.com",
        phone: "555-234-5678"
      }
    },
    {
      username: "dr.williams",
      legalName: {
        firstName: "David",
        lastName: "Williams"
      },
      medicalId: "MD24680",
      specialization: "Neurology",
      biography: "Dr. Williams is a neurologist focusing on cognitive disorders, headaches, and stroke treatment with advanced training in neuroscience.",
      availableSlots: [
        { dayOfWeek: "Monday", startTime: "08:30", endTime: "16:30" },
        { dayOfWeek: "Wednesday", startTime: "08:30", endTime: "16:30" },
        { dayOfWeek: "Thursday", startTime: "08:30", endTime: "16:30" }
      ],
      breakTimes: [
        { dayOfWeek: "Monday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Wednesday", startTime: "12:00", endTime: "13:00" },
        { dayOfWeek: "Thursday", startTime: "12:00", endTime: "13:00" }
      ],
      contactInfo: {
        email: "david.williams@medicalmanager.com",
        phone: "555-345-6789"
      }
    }
  ]);