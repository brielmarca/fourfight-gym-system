import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/termos-utilizacao")({
  component: TermosUtilizacaoPage,
});

function TermosUtilizacaoPage() {
  return (
    <main className="min-h-screen bg-background px-4 pb-16 pt-28 text-foreground sm:px-6 lg:px-8">
      <div className="mx-auto max-w-4xl">
        <h1 className="text-3xl font-semibold text-white sm:text-4xl">Termos de Utilização</h1>
        <div className="mt-6 space-y-6 text-sm leading-7 text-zinc-300">
          <section>
            <h2 className="text-xl font-semibold text-white">1. Objeto</h2>
            <p className="mt-2">
              Estes termos regulam a utilização do website e da plataforma da 4Four Fight Academy.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">2. Dados pessoais</h2>
            <p className="mt-2">
              Os dados fornecidos são utilizados para criação e gestão de conta, adesão,
              comunicação, contacto e gestão da atividade do ginásio.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">3. Pagamentos</h2>
            <p className="mt-2">
              Os pagamentos online são processados pela Stripe. A aplicação da academia não armazena
              dados completos de cartão.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">4. Segurança e acesso</h2>
            <p className="mt-2">
              O utilizador é responsável por manter as suas credenciais em segurança e por não as
              partilhar com terceiros.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">5. Retificação e eliminação</h2>
            <p className="mt-2">
              O utilizador pode solicitar correção ou eliminação dos seus dados pessoais através do
              contacto oficial da academia: 4fourfight@gmail.com.
            </p>
          </section>
        </div>
      </div>
    </main>
  );
}
