-- Replace the public legacy catalog while preserving every historical plan and membership.
-- Stripe product/price IDs intentionally remain NULL until real Stripe Prices are configured.

UPDATE plans
SET is_active = false,
    updated_at = CURRENT_TIMESTAMP
WHERE name IN ('Basic', 'Standard', 'Premium')
  AND is_active = true
  AND deleted_at IS NULL;

INSERT INTO plans (
    id,
    name,
    description,
    price,
    currency,
    duration_days,
    max_classes,
    features,
    level,
    is_active,
    stripe_price_id,
    stripe_product_id
)
VALUES
    (
        '4f280001-0000-4000-8000-000000000001',
        'Sócio Fundador — Mensalidade 1 Modalidade — Adulto',
        'Plano Sócio Fundador para adulto com acesso a uma modalidade.',
        38.50, 'EUR', 30, NULL,
        '["Adulto", "1 modalidade", "Plano Sócio Fundador"]'::jsonb,
        'Adulto', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000002',
        'Sócio Fundador — Livre — Adulto',
        'Plano Sócio Fundador para adulto com acesso livre às modalidades.',
        63.00, 'EUR', 30, NULL,
        '["Adulto", "Modalidades livres", "Plano Sócio Fundador"]'::jsonb,
        'Adulto', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000003',
        'Sócio Fundador — Kids 1 Modalidade — Kids',
        'Plano Sócio Fundador Kids com acesso a uma modalidade.',
        24.50, 'EUR', 30, NULL,
        '["Kids", "1 modalidade", "Plano Sócio Fundador"]'::jsonb,
        'Kids', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000004',
        'Sócio Fundador — Kids 2 Modalidades — Kids',
        'Plano Sócio Fundador Kids com acesso a duas modalidades.',
        38.50, 'EUR', 30, NULL,
        '["Kids", "2 modalidades", "Plano Sócio Fundador"]'::jsonb,
        'Kids', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000005',
        'Mensalidade 1 Modalidade — Adulto',
        'Plano normal para adulto com acesso a uma modalidade.',
        55.00, 'EUR', 30, NULL,
        '["Adulto", "1 modalidade", "Plano Normal"]'::jsonb,
        'Adulto', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000006',
        'Mensalidade 2 Modalidades — Adulto',
        'Plano normal para adulto com acesso a duas modalidades.',
        75.00, 'EUR', 30, NULL,
        '["Adulto", "2 modalidades", "Plano Normal"]'::jsonb,
        'Adulto', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000007',
        'Livre — Adulto',
        'Plano normal para adulto com acesso livre às modalidades.',
        90.00, 'EUR', 30, NULL,
        '["Adulto", "Modalidades livres", "Plano Normal"]'::jsonb,
        'Adulto', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000008',
        'Kids 1 Modalidade — Kids',
        'Plano normal Kids com acesso a uma modalidade.',
        35.00, 'EUR', 30, NULL,
        '["Kids", "1 modalidade", "Plano Normal"]'::jsonb,
        'Kids', true, NULL, NULL
    ),
    (
        '4f280001-0000-4000-8000-000000000009',
        'Kids 2 Modalidades — Kids',
        'Plano normal Kids com acesso a duas modalidades.',
        55.00, 'EUR', 30, NULL,
        '["Kids", "2 modalidades", "Plano Normal"]'::jsonb,
        'Kids', true, NULL, NULL
    )
ON CONFLICT (id) DO NOTHING;
