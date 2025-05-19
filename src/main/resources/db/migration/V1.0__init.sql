-- Create legal cases table
CREATE TABLE legal_cases
(
    id          VARCHAR(36) PRIMARY KEY,
    title       VARCHAR(255) NOT NULL,
    description VARCHAR(2000),
    client_id   VARCHAR(36)  NOT NULL,
    profile_id  VARCHAR(36)  NOT NULL,
    status      VARCHAR(50)  NOT NULL,
    start_date  TIMESTAMP,
    end_date    TIMESTAMP,
    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     BOOLEAN               DEFAULT FALSE,
    deleted_at  TIMESTAMP,
    deleted_by  VARCHAR(36)
);

-- Create indexes for legal cases
CREATE INDEX idx_legal_cases_profile_id ON legal_cases (profile_id);
CREATE INDEX idx_legal_cases_client_id ON legal_cases (client_id);
CREATE INDEX idx_legal_cases_status ON legal_cases (status);

-- Create case documents table with single table inheritance
CREATE TABLE case_documents
(
    id                    VARCHAR(36) PRIMARY KEY,
    title                 VARCHAR(255)  NOT NULL,
    description           VARCHAR(1000),
    file_path             VARCHAR(1000) NOT NULL,
    file_type             VARCHAR(100),
    file_size             BIGINT,
    storage_id            VARCHAR(255),
    case_id               VARCHAR(36)   NOT NULL,
    document_type         VARCHAR(50),
    status                VARCHAR(50),
    category              VARCHAR(50),
    document_category     VARCHAR(50)   NOT NULL,
    created_by            VARCHAR(36),
    updated_by            VARCHAR(36),
    created_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at            TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               BOOLEAN                DEFAULT FALSE,
    deleted_at            TIMESTAMP,
    deleted_by            VARCHAR(36),
    metadata_json JSONB,

    -- Fields for QuestionnaireDocument
    questionnaire_type    VARCHAR(100),
    completion_percentage INTEGER,
    responses_json JSONB,

    -- Fields for ProfileDocument
    profile_type          VARCHAR(100),
    identity_verified     BOOLEAN,
    verification_method   VARCHAR(100),

    -- Fields for SupportingDocument
    document_reference    VARCHAR(255),
    issuing_authority     VARCHAR(255),
    issue_date            TIMESTAMP,
    expiry_date           TIMESTAMP,
    verification_required BOOLEAN,
    verified              BOOLEAN
);

-- Create indexes for case documents
CREATE INDEX idx_case_documents_case_id ON case_documents (case_id);
CREATE INDEX idx_case_documents_document_type ON case_documents (document_type);
CREATE INDEX idx_case_documents_status ON case_documents (status);
CREATE INDEX idx_case_documents_category ON case_documents (document_category);

CREATE TABLE event_logs
(
    id          VARCHAR(36) PRIMARY KEY,
    case_id     VARCHAR(36),
    event_id    VARCHAR(36)  NOT NULL,
    event_type  VARCHAR(100) NOT NULL,
    occurred_at TIMESTAMP    NOT NULL,
    event_data JSONB NOT NULL,

    created_by  VARCHAR(36),
    updated_by  VARCHAR(36),
    created_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX idx_event_logs_case_id ON event_logs (case_id);
CREATE INDEX idx_event_logs_event_type ON event_logs (event_type);
CREATE INDEX idx_event_logs_occurred_at ON event_logs (occurred_at);

ALTER TABLE event_logs
    ADD CONSTRAINT fk_event_logs_case_id
        FOREIGN KEY (case_id)
            REFERENCES legal_cases (id)
            ON DELETE CASCADE; 