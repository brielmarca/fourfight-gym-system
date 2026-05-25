import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/politica-cookies")({
  component: PoliticaCookiesPage,
});

function PoliticaCookiesPage() {
  return (
    <main className="min-h-screen bg-background px-4 pb-16 pt-28 text-foreground sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        <h1 className="text-3xl font-semibold text-white sm:text-4xl">Política de Cookies</h1>

        <div className="mt-8 space-y-8 text-sm leading-7 text-zinc-300">
          <section>
            <h2 className="text-xl font-semibold text-white">O que são cookies</h2>
            <p className="mt-2">
              Cookies são pequenos ficheiros guardados no teu dispositivo que ajudam o site a funcionar,
              manter sessões seguras e recordar escolhas, como as preferências de cookies.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">Que tipos de cookies usamos</h2>
            <p className="mt-2">
              Utilizamos cookies estritamente necessários e, apenas com consentimento, cookies
              analíticos para compreender a utilização do site.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">Cookies estritamente necessários</h2>
            <p className="mt-2">
              Estes cookies suportam autenticação, segurança, navegação e o registo da tua escolha de
              consentimento. Sem estes cookies, partes essenciais do serviço podem não funcionar.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">Cookies analíticos</h2>
            <p className="mt-2">
              São opcionais e só são ativados quando o utilizador aceita. Servem para analisar padrões de
              navegação e melhorar conteúdos, modalidades e experiência.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">Como gerir preferências</h2>
            <p className="mt-2">
              Podes aceitar, rejeitar ou personalizar cookies no banner inicial. Também podes reabrir as
              preferências em "Preferências de cookies" no rodapé e retirar consentimento a qualquer
              momento.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">Contacto</h2>
            <p className="mt-2">Para questões sobre privacidade e cookies, contacta: 4fourfight@gmail.com</p>
          </section>
        </div>

        <div className="mt-8 overflow-x-auto rounded-lg border border-zinc-800">
          <table className="min-w-full divide-y divide-zinc-800 text-left text-sm text-zinc-200">
            <thead className="bg-zinc-950/80 text-xs uppercase tracking-wider text-zinc-400">
              <tr>
                <th className="px-4 py-3">Category</th>
                <th className="px-4 py-3">Purpose</th>
                <th className="px-4 py-3">Examples</th>
                <th className="px-4 py-3">Consent required</th>
                <th className="px-4 py-3">Retention</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-800">
              <tr>
                <td className="px-4 py-4 font-medium text-white">Estritamente necessários</td>
                <td className="px-4 py-4">
                  Permitem autenticação, segurança, navegação e guardar a escolha de consentimento.
                </td>
                <td className="px-4 py-4">Sessão/autenticação, proteção de segurança, fourfight_cookie_consent_v1</td>
                <td className="px-4 py-4">Não, quando estritamente necessários ao serviço pedido.</td>
                <td className="px-4 py-4">
                  Durante a sessão ou até alteração das preferências, conforme o caso.
                </td>
              </tr>
              <tr>
                <td className="px-4 py-4 font-medium text-white">Analíticos</td>
                <td className="px-4 py-4">
                  Ajudam a compreender como o site é utilizado para melhorar páginas, modalidades e
                  experiência.
                </td>
                <td className="px-4 py-4">Google Analytics 4, se configurado</td>
                <td className="px-4 py-4">Sim</td>
                <td className="px-4 py-4">
                  Até 14 meses, se configurado no Google Analytics. Confirmar em revisão legal/configuração
                  GA.
                </td>
              </tr>
            </tbody>
          </table>
        </div>

        <p className="mt-6 rounded-md border border-amber-600/50 bg-amber-950/30 p-4 text-sm text-amber-200">
          Esta política deve ser revista pelo responsável legal/privacidade da 4Four Fight Academy antes
          da entrada em produção.
        </p>

        {/* TODO(legal): Validar redação final RGPD/ePrivacy e retenções com responsável legal. */}
        {/* TODO(legal): Confirmar base legal, fornecedores e lista final de cookies por ambiente. */}
      </div>
    </main>
  );
}
