CREATE TABLE outbox
(
    id             UUID PRIMARY KEY,
    aggregate_id   UUID                                          NOT NULL,
    aggregate_type VARCHAR(255)                                  NOT NULL,
    event_type     VARCHAR(255)                                  NOT NULL,
    payload        TEXT                                          NOT NULL,
    event_time     TIMESTAMP WITHOUT TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    status         VARCHAR(50)                 DEFAULT 'PENDING' NOT NULL
);
