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
        'Ideal para iniciantes que desejam começar sua jornada fitness com uma base sólida. Este plano oferece acesso essencial à academia com equipamentos de musculação, permitindo que você estabeleça uma rotina de exercícios consistente. Perfeito para quem busca qualidade com custo acessível.',
        29.90,
        'EUR',
        30,
        8,
        '["Acesso à academia", "Musculação livre", "Vestiários completos", "App de treinos básico", "Suporte inicial de orientação"]'::jsonb,
        true
    ),
    (
        gen_random_uuid(),
        'Standard',
        'Perfeito para praticantes regulares que desejam variedade em seus treinos. Além do acesso completo à academia, você conta com aulas coletivas (limitadas a 20 por mês), avaliação física mensal para acompanhar seu progresso, e uma sessão mensal com personal trainer para otimizar seus resultados.',
        49.90,
        'EUR',
        30,
        20,
        '["Acesso à academia", "Musculação livre", "Aulas coletivas (até 20/mês)", "Avaliação física mensal", "Vestiários completos", "App de treinos premium", "1 sessão mensal com personal", "Acesso à área de descanso"]'::jsonb,
        true
    ),
    (
        gen_random_uuid(),
        'Premium',
        'A experiência definitiva para atletas dedicados. Desfrute de acesso 24 horas, aulas coletivas ilimitadas, avaliação física semanal personalizada, e benefícios exclusivos como nutricionista incluso, vestiários premium com sauna, e convites para eventos especiais. O plano mais completo para quem não abre mão de excelência.',
        79.90,
        'EUR',
        30,
        100,
        '["Acesso à academia 24h", "Musculação livre", "Aulas coletivas ilimitadas", "Avaliação física semanal", "Sala de espera para crianças", "Área de café e lounge", "2 sessões mensais com personal", "Nutricionista incluso", "Vestiários premium com sauna", "App de treinos elite", "Convites para eventos exclusivos", "Estacionamento parceiro", "Toalhas premium inclusas"]'::jsonb,
        true
    )
ON CONFLICT DO NOTHING;
