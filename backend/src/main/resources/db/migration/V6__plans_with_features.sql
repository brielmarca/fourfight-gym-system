-- V6__plans_with_features.sql
-- Add features column to plans table and create 3 standard plans

-- Add features column as JSONB to store plan features
ALTER TABLE plans ADD COLUMN IF NOT EXISTS features JSONB;

-- Deactivate old plans
UPDATE plans SET is_active = false, updated_at = CURRENT_TIMESTAMP WHERE is_active = true;

-- Insert 3 standard plans with features (prices in EUR)
INSERT INTO plans (id, name, description, price, currency, duration_days, max_classes, features, is_active)
VALUES
    (
        gen_random_uuid(),
        'Basic',
        'Ideal para iniciantes que desejam comecar sua jornada fitness com uma base solida. Este plano oferece acesso essencial a academia com equipamentos de musculacao, permitindo que voce estabeleca uma rotina de exercicios consistente. Perfeito para quem busca qualidade com custo acessivel.',
        29.90,
        'EUR',
        30,
        8,
        '["Acesso a academia", "Musculacao livre", "Vestuarios completos", "App de treinos basico", "Suporte inicial de orientacao"]'::jsonb,
        true
    ),
    (
        gen_random_uuid(),
        'Standard',
        'Perfeito para praticantes regulares que desejam variedade em seus treinos. Alem do acesso completo a academia, voce conta com aulas coletivas (limitadas a 20 por mes), avaliacao fisica mensal para acompanhar seu progresso, e uma sessao mensal com personal trainer para otimizar seus resultados.',
        49.90,
        'EUR',
        30,
        20,
        '["Acesso a academia", "Musculacao livre", "Aulas coletivas (ate 20/mes)", "Avaliacao fisica mensal", "Vestuarios completos", "App de treinos premium", "1 sessao mensal com personal", "Acesso a area de descanso"]'::jsonb,
        true
    ),
    (
        gen_random_uuid(),
        'Premium',
        'A experiencia definitiva para atletas dedicados. Desfrute de acesso 24 horas, aulas coletivas ilimitadas, avaliacao fisica semanal personalizada, e beneficios exclusivos como nutricionista incluso, vestuarios premium com sauna, e convites para eventos especiais. O plano mais completo para quem nao abre mao de excelencia.',
        79.90,
        'EUR',
        30,
        100,
        '["Acesso a academia 24h", "Musculacao livre", "Aulas coletivas ilimitadas", "Avaliacao fisica semanal", "Sala de espera para criancas", "Area de cafe e lounge", "2 sessoes mensais com personal", "Nutricionista incluso", "Vestuarios premium com sauna", "App de treinos elite", "Convites para eventos exclusivos", "Estacionamento parceiro", "Toalhas premium inclusas"]'::jsonb,
        true
    )
ON CONFLICT DO NOTHING;
