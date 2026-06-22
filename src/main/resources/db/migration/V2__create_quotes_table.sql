CREATE TABLE quotes (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    customer_id BIGINT NOT NULL,
    insurance_type VARCHAR(30) NOT NULL,
    coverage_amount NUMERIC(15, 2) NOT NULL,
    premium NUMERIC(15, 2) NOT NULL,
    status VARCHAR(20) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_quotes_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX idx_quotes_customer_id ON quotes (customer_id);
CREATE INDEX idx_quotes_status ON quotes (status);
