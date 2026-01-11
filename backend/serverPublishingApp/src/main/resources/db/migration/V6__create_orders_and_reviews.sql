CREATE TABLE orders (
    id SERIAL PRIMARY KEY,
    user_id INT NOT NULL REFERENCES users(id),
    service_id INT NOT NULL REFERENCES publishing_services(id),
    status VARCHAR(50) NOT NULL DEFAULT 'created'
        CHECK (status IN (
            'created',
            'under_review',
            'editing',
            'ready_for_print',
            'completed',
            'canceled'
        )),
    total_price NUMERIC(10,2) NOT NULL DEFAULT 0,
    pages INT DEFAULT NULL,
    quantity INT DEFAULT 1,
    created_at TIMESTAMP DEFAULT NOW(),
    updated_at TIMESTAMP DEFAULT NOW()
);


CREATE TABLE order_materials (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    material_id INT NOT NULL REFERENCES materials(id),
    quantity INT NOT NULL DEFAULT 1,
    price NUMERIC(10,2) NOT NULL
);

CREATE TABLE order_files (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_path VARCHAR(500) NOT NULL,
    uploaded_at TIMESTAMP DEFAULT NOW()
);

CREATE TABLE reviews (
    id SERIAL PRIMARY KEY,
    order_id INT NOT NULL REFERENCES orders(id) ON DELETE CASCADE,
    reviewer_id INT NOT NULL REFERENCES users(id),
    comment TEXT,
    status VARCHAR(50) NOT NULL DEFAULT 'pending'
        CHECK (status IN ('pending', 'approved', 'rejected')),
    created_at TIMESTAMP DEFAULT NOW()
);

