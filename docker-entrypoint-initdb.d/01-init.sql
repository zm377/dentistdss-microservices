

Enum "user_role" {
  "SYSTEM_ADMIN"
  "CLINIC_ADMIN"
  "DENTIST"
  "RECEPTIONIST"
  "PATIENT"
}

Enum "user_provider" {
  "LOCAL"
  "GOOGLE"
}

Enum "appointment_status" {
  "REQUESTED"
  "CONFIRMED"
  "CANCELLED"
  "COMPLETED"
  "NO_SHOW"
  "RESCHEDULED"
}

Enum "urgency_level" {
  "ROUTINE"
  "MODERATE"
  "URGENT"
  "EMERGENCY"
}

Enum "notification_type" {
  "EMAIL"
  "SMS"
  "PUSH"
  "IN_APP"
}

Enum "notification_status" {
  "PENDING"
  "SENT"
  "FAILED"
  "READ"
}

Enum "approval_status" {
  "PENDING"
  "APPROVED"
  "REJECTED"
}

Table "users" {
  "id" BIGINT [pk, default: `nextval('user_id_seq')`]
  "email" VARCHAR(255) [unique, not null]
  "password" VARCHAR(255)
  "first_name" VARCHAR(100) [not null]
  "last_name" VARCHAR(100) [not null]
  "phone" VARCHAR(20)
  "date_of_birth" DATE
  "address" TEXT
  "provider" user_provider [default: 'LOCAL']
  "google_id" VARCHAR(255)
  "profile_picture_url" VARCHAR(500)
  "email_verified" BOOLEAN [default: FALSE]
  "phone_verified" BOOLEAN [default: FALSE]
  "enabled" BOOLEAN [default: TRUE]
  "account_non_expired" BOOLEAN [default: TRUE]
  "credentials_non_expired" BOOLEAN [default: TRUE]
  "account_non_locked" BOOLEAN [default: TRUE]
  "last_login_at" TIMESTAMP
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "updated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]

  Indexes {
    email [name: "idx_users_email"]
    provider [name: "idx_users_provider"]
  }
}

Table "user_roles" {
  "user_id" BIGINT
  "role" user_role [not null]
  "assigned_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "assigned_by" BIGINT

  Indexes {
    (user_id, role) [pk]
    user_id [name: "idx_user_roles_user_id"]
    role [name: "idx_user_roles_role"]
  }
}

Table "clinics" {
  "id" BIGINT [pk, default: `nextval('clinic_id_seq')`]
  "name" VARCHAR(255) [not null]
  "address" TEXT [not null]
  "phone" VARCHAR(20) [not null]
  "email" VARCHAR(255)
  "website" VARCHAR(255)
  "tax_id" VARCHAR(50)
  "license_number" VARCHAR(100)
  "established_date" DATE
  "description" TEXT
  "logo_url" VARCHAR(500)
  "active" BOOLEAN [default: TRUE]
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "updated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "clinic_operating_hours" {
  "id" SERIAL [pk, increment]
  "clinic_id" BIGINT
  "day_of_week" INTEGER
  "open_time" TIME [not null]
  "close_time" TIME [not null]
  "is_closed" BOOLEAN [default: FALSE]

  Indexes {
    (clinic_id, day_of_week) [unique]
  }
}

Table "clinic_holidays" {
  "id" SERIAL [pk, increment]
  "clinic_id" BIGINT
  "holiday_date" DATE [not null]
  "description" VARCHAR(255)

  Indexes {
    (clinic_id, holiday_date) [unique]
  }
}

Table "user_clinics" {
  "user_id" BIGINT
  "clinic_id" BIGINT
  "role" user_role [not null]
  "specialty" VARCHAR(100)
  "license_number" VARCHAR(100)
  "start_date" DATE [default: `CURRENT_DATE`]
  "end_date" DATE
  "is_active" BOOLEAN [default: TRUE]

  Indexes {
    (user_id, clinic_id) [pk]
    user_id [name: "idx_user_clinics_user_id"]
    clinic_id [name: "idx_user_clinics_clinic_id"]
  }
}

Table "patient_profiles" {
  "user_id" BIGINT [pk]
  "emergency_contact_name" VARCHAR(200)
  "emergency_contact_phone" VARCHAR(20)
  "emergency_contact_relationship" VARCHAR(50)
  "insurance_provider" VARCHAR(200)
  "insurance_policy_number" VARCHAR(100)
  "allergies" "TEXT[]"
  "medical_conditions" "TEXT[]"
  "current_medications" "TEXT[]"
  "preferred_pharmacy" VARCHAR(255)
  "blood_type" VARCHAR(10)
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "updated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "medical_history" {
  "id" SERIAL [pk, increment]
  "patient_id" BIGINT
  "condition_name" VARCHAR(255) [not null]
  "diagnosed_date" DATE
  "status" VARCHAR(50)
  "notes" TEXT
  "created_by" BIGINT
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "services" {
  "id" SERIAL [pk, increment]
  "clinic_id" BIGINT
  "name" VARCHAR(255) [not null]
  "description" TEXT
  "duration_minutes" INTEGER [default: 30]
  "price" DECIMAL(10,2)
  "category" VARCHAR(100)
  "is_active" BOOLEAN [default: TRUE]
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "appointments" {
  "id" BIGINT [pk, default: `nextval('appointment_id_seq')`]
  "patient_id" BIGINT
  "dentist_id" BIGINT
  "clinic_id" BIGINT
  "service_id" INTEGER
  "appointment_date" DATE [not null]
  "start_time" TIME [not null]
  "end_time" TIME [not null]
  "status" appointment_status [default: 'REQUESTED']
  "reason_for_visit" TEXT
  "symptoms" TEXT
  "urgency" urgency_level [default: 'ROUTINE']
  "ai_triage_notes" TEXT
  "notes" TEXT
  "created_by" BIGINT
  "confirmed_by" BIGINT
  "cancelled_by" BIGINT
  "cancellation_reason" TEXT
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "updated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]

  Indexes {
    patient_id [name: "idx_appointments_patient_id"]
    dentist_id [name: "idx_appointments_dentist_id"]
    clinic_id [name: "idx_appointments_clinic_id"]
    appointment_date [name: "idx_appointments_date"]
    status [name: "idx_appointments_status"]
  }
}

Table "appointment_history" {
  "id" SERIAL [pk, increment]
  "appointment_id" BIGINT
  "old_status" appointment_status
  "new_status" appointment_status
  "old_date" DATE
  "new_date" DATE
  "old_time" TIME
  "new_time" TIME
  "change_reason" TEXT
  "changed_by" BIGINT
  "changed_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "dentist_availability" {
  "id" SERIAL [pk, increment]
  "dentist_id" BIGINT
  "clinic_id" BIGINT
  "available_date" DATE [not null]
  "start_time" TIME [not null]
  "end_time" TIME [not null]
  "is_blocked" BOOLEAN [default: FALSE]
  "block_reason" VARCHAR(255)
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]

  Indexes {
    (dentist_id, clinic_id, available_date, start_time) [unique]
  }
}

Table "appointment_waitlist" {
  "id" SERIAL [pk, increment]
  "patient_id" BIGINT
  "clinic_id" BIGINT
  "service_id" INTEGER
  "preferred_dentist_id" BIGINT
  "preferred_date_from" DATE
  "preferred_date_to" DATE
  "preferred_time_slot" VARCHAR(50)
  "urgency" urgency_level [default: 'ROUTINE']
  "notes" TEXT
  "is_active" BOOLEAN [default: TRUE]
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "clinical_notes" {
  "id" BIGINT [pk, default: `nextval('patient_record_id_seq')`]
  "appointment_id" BIGINT
  "patient_id" BIGINT
  "dentist_id" BIGINT
  "clinic_id" BIGINT
  "chief_complaint" TEXT
  "examination_findings" TEXT
  "diagnosis" TEXT
  "treatment_performed" TEXT
  "treatment_plan" TEXT
  "prescriptions" TEXT
  "follow_up_instructions" TEXT
  "ai_assisted_notes" TEXT
  "attachments" "TEXT[]"
  "is_draft" BOOLEAN [default: FALSE]
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "updated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "signed_at" TIMESTAMP
  "signed_by" BIGINT

  Indexes {
    patient_id [name: "idx_clinical_notes_patient_id"]
    appointment_id [name: "idx_clinical_notes_appointment_id"]
  }
}

Table "treatment_plans" {
  "id" SERIAL [pk, increment]
  "patient_id" BIGINT
  "dentist_id" BIGINT
  "clinic_id" BIGINT
  "plan_name" VARCHAR(255)
  "description" TEXT
  "total_cost" DECIMAL(10,2)
  "insurance_coverage" DECIMAL(10,2)
  "patient_cost" DECIMAL(10,2)
  "status" VARCHAR(50) [default: 'PROPOSED']
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "accepted_at" TIMESTAMP
  "completed_at" TIMESTAMP
}

Table "treatment_plan_items" {
  "id" SERIAL [pk, increment]
  "treatment_plan_id" INTEGER
  "service_id" INTEGER
  "tooth_number" VARCHAR(10)
  "description" TEXT
  "cost" DECIMAL(10,2)
  "status" VARCHAR(50) [default: 'PENDING']
  "sequence_order" INTEGER
  "notes" TEXT
}

Table "ai_interactions" {
  "id" SERIAL [pk, increment]
  "user_id" BIGINT
  "session_id" UUID [not null]
  "interaction_type" VARCHAR(50)
  "ai_model" VARCHAR(50)
  "input_text" TEXT
  "ai_response" TEXT
  "tokens_used" INTEGER
  "response_time_ms" INTEGER
  "feedback_rating" INTEGER
  "feedback_comment" TEXT
  "metadata" JSONB
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]

  Indexes {
    user_id [name: "idx_ai_interactions_user_id"]
    session_id [name: "idx_ai_interactions_session_id"]
    created_at [name: "idx_ai_interactions_created_at"]
  }
}

Table "ai_triage_assessments" {
  "id" SERIAL [pk, increment]
  "patient_id" BIGINT
  "session_id" UUID [not null]
  "symptoms" TEXT [not null]
  "ai_questions" JSONB
  "urgency_assessment" urgency_level
  "recommended_action" TEXT
  "disclaimer_shown" BOOLEAN [default: TRUE]
  "resulted_in_appointment" BIGINT
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "notification_templates" {
  "id" SERIAL [pk, increment]
  "name" VARCHAR(255) [unique, not null]
  "type" notification_type [not null]
  "subject" VARCHAR(255)
  "body_template" TEXT [not null]
  "variables" "TEXT[]"
  "is_active" BOOLEAN [default: TRUE]
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "notifications" {
  "id" BIGINT [pk, default: `nextval('notification_id_seq')`]
  "user_id" BIGINT
  "template_id" INTEGER
  "type" notification_type [not null]
  "subject" VARCHAR(255)
  "body" TEXT [not null]
  "status" notification_status [default: 'PENDING']
  "scheduled_for" TIMESTAMP
  "sent_at" TIMESTAMP
  "read_at" TIMESTAMP
  "metadata" JSONB
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]

  Indexes {
    user_id [name: "idx_notifications_user_id"]
    status [name: "idx_notifications_status"]
    scheduled_for [name: "idx_notifications_scheduled_for"]
  }
}

Table "user_approval_requests" {
  "id" SERIAL [pk, increment]
  "user_id" BIGINT
  "requested_role" user_role [not null]
  "clinic_id" BIGINT
  "status" approval_status [default: 'PENDING']
  "request_reason" TEXT
  "supporting_documents" "TEXT[]"
  "reviewed_by" BIGINT
  "review_notes" TEXT
  "reviewed_at" TIMESTAMP
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
  "updated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "audit_logs" {
  "id" BIGINT [pk, default: `nextval('audit_log_id_seq')`]
  "user_id" BIGINT
  "action" VARCHAR(255) [not null]
  "entity_type" VARCHAR(100)
  "entity_id" BIGINT
  "old_values" JSONB
  "new_values" JSONB
  "ip_address" INET
  "user_agent" TEXT
  "clinic_id" BIGINT
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]

  Indexes {
    user_id [name: "idx_audit_logs_user_id"]
    (entity_type, entity_id) [name: "idx_audit_logs_entity"]
    created_at [name: "idx_audit_logs_created_at"]
  }
}

Table "data_access_logs" {
  "id" SERIAL [pk, increment]
  "user_id" BIGINT
  "patient_id" BIGINT
  "access_type" VARCHAR(50)
  "resource_type" VARCHAR(100)
  "resource_id" BIGINT
  "access_reason" TEXT
  "ip_address" INET
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "report_definitions" {
  "id" SERIAL [pk, increment]
  "name" VARCHAR(255) [not null]
  "description" TEXT
  "report_type" VARCHAR(100)
  "query_template" TEXT
  "parameters" JSONB
  "roles_allowed" "user_role[]"
  "is_active" BOOLEAN [default: TRUE]
  "created_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Table "generated_reports" {
  "id" SERIAL [pk, increment]
  "report_definition_id" INTEGER
  "generated_by" BIGINT
  "clinic_id" BIGINT
  "parameters" JSONB
  "result_data" JSONB
  "file_url" VARCHAR(500)
  "generated_at" TIMESTAMP [default: `CURRENT_TIMESTAMP`]
}

Ref:"users"."id" < "user_roles"."user_id" [delete: cascade]

Ref:"users"."id" < "user_roles"."assigned_by"

Ref:"clinics"."id" < "clinic_operating_hours"."clinic_id" [delete: cascade]

Ref:"clinics"."id" < "clinic_holidays"."clinic_id" [delete: cascade]

Ref:"users"."id" < "user_clinics"."user_id" [delete: cascade]

Ref:"clinics"."id" < "user_clinics"."clinic_id" [delete: cascade]

Ref:"users"."id" < "patient_profiles"."user_id" [delete: cascade]

Ref:"users"."id" < "medical_history"."patient_id" [delete: cascade]

Ref:"users"."id" < "medical_history"."created_by"

Ref:"clinics"."id" < "services"."clinic_id" [delete: cascade]

Ref:"users"."id" < "appointments"."patient_id" [delete: cascade]

Ref:"users"."id" < "appointments"."dentist_id" [delete: cascade]

Ref:"clinics"."id" < "appointments"."clinic_id" [delete: cascade]

Ref:"services"."id" < "appointments"."service_id"

Ref:"users"."id" < "appointments"."created_by"

Ref:"users"."id" < "appointments"."confirmed_by"

Ref:"users"."id" < "appointments"."cancelled_by"

Ref:"appointments"."id" < "appointment_history"."appointment_id" [delete: cascade]

Ref:"users"."id" < "appointment_history"."changed_by"

Ref:"users"."id" < "dentist_availability"."dentist_id" [delete: cascade]

Ref:"clinics"."id" < "dentist_availability"."clinic_id" [delete: cascade]

Ref:"users"."id" < "appointment_waitlist"."patient_id" [delete: cascade]

Ref:"clinics"."id" < "appointment_waitlist"."clinic_id" [delete: cascade]

Ref:"services"."id" < "appointment_waitlist"."service_id"

Ref:"users"."id" < "appointment_waitlist"."preferred_dentist_id"

Ref:"appointments"."id" < "clinical_notes"."appointment_id"

Ref:"users"."id" < "clinical_notes"."patient_id" [delete: cascade]

Ref:"users"."id" < "clinical_notes"."dentist_id" [delete: cascade]

Ref:"clinics"."id" < "clinical_notes"."clinic_id" [delete: cascade]

Ref:"users"."id" < "clinical_notes"."signed_by"

Ref:"users"."id" < "treatment_plans"."patient_id" [delete: cascade]

Ref:"users"."id" < "treatment_plans"."dentist_id" [delete: cascade]

Ref:"clinics"."id" < "treatment_plans"."clinic_id" [delete: cascade]

Ref:"treatment_plans"."id" < "treatment_plan_items"."treatment_plan_id" [delete: cascade]

Ref:"services"."id" < "treatment_plan_items"."service_id"

Ref:"users"."id" < "ai_interactions"."user_id"

Ref:"users"."id" < "ai_triage_assessments"."patient_id" [delete: cascade]

Ref:"appointments"."id" < "ai_triage_assessments"."resulted_in_appointment"

Ref:"users"."id" < "notifications"."user_id" [delete: cascade]

Ref:"notification_templates"."id" < "notifications"."template_id"

Ref:"users"."id" < "user_approval_requests"."user_id" [delete: cascade]

Ref:"clinics"."id" < "user_approval_requests"."clinic_id"

Ref:"users"."id" < "user_approval_requests"."reviewed_by"

Ref:"users"."id" < "audit_logs"."user_id"

Ref:"clinics"."id" < "audit_logs"."clinic_id"

Ref:"users"."id" < "data_access_logs"."user_id"

Ref:"users"."id" < "data_access_logs"."patient_id"

Ref:"report_definitions"."id" < "generated_reports"."report_definition_id"

Ref:"users"."id" < "generated_reports"."generated_by"

Ref:"clinics"."id" < "generated_reports"."clinic_id"
