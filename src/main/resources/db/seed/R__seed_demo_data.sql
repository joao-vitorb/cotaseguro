INSERT INTO customers (name, document, birth_date, email)
VALUES
    ('Alice Martins', '11111111111', '1990-04-12', 'alice.martins@example.com'),
    ('Bruno Carvalho', '22222222222', '1985-09-30', 'bruno.carvalho@example.com'),
    ('Carla Souza', '33333333333', '2001-01-05', 'carla.souza@example.com')
ON CONFLICT (document) DO NOTHING;
