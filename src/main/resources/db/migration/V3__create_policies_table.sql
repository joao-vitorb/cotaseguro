CREATE TABLE policies (
    id BIGINT GENERATED ALWAYS AS IDENTITY PRIMARY KEY,
    quote_id BIGINT NOT NULL,
    customer_id BIGINT NOT NULL,
    number VARCHAR(50) NOT NULL,
    status VARCHAR(20) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT uk_policies_quote UNIQUE (quote_id),
    CONSTRAINT uk_policies_number UNIQUE (number),
    CONSTRAINT fk_policies_quote FOREIGN KEY (quote_id) REFERENCES quotes (id),
    CONSTRAINT fk_policies_customer FOREIGN KEY (customer_id) REFERENCES customers (id)
);

CREATE INDEX idx_policies_customer_id ON policies (customer_id);
CREATE INDEX idx_policies_status ON policies (status);
