import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/politica-cookies")({
  component: PoliticaCookiesPage,
});

function PoliticaCookiesPage() {
  return (
    <main className="min-h-screen bg-background px-4 pb-16 pt-28 text-foreground sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        <h1 className="text-3xl font-semibold text-white sm:text-4xl">Política de Cookies</h1>
        <p className="mt-4 text-sm leading-7 text-zinc-300">Última atualização: 27 de maio de 2026.</p>

        <div className="mt-8 space-y-8 text-sm leading-7 text-zinc-300">
          <section>
            <h2 className="text-xl font-semibold text-white">1. O que são cookies</h2>
            <p className="mt-2">
              Cookies são pequenos ficheiros guardados no dispositivo que ajudam o site a funcionar,
              manter sessões seguras e recordar preferências.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">2. Tipos de cookies utilizados</h2>
            <p className="mt-2">
              O site utiliza cookies estritamente necessários para login, sessão, segurança e gestão de
              preferências de cookies. Cookies de preferências, analíticos ou marketing só são
              aplicados quando tecnicamente configurados e, quando exigido, após consentimento.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">3. Cookies estritamente necessários</h2>
            <p className="mt-2">
              São essenciais para autenticação, segurança, navegação e memorizar a escolha de
              consentimento. Sem estes cookies, funcionalidades críticas do site podem deixar de
              funcionar, incluindo acesso à conta e áreas reservadas.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">4. Cookies analíticos</h2>
            <p className="mt-2">
              Não são necessários ao funcionamento base do serviço. No projeto 4Four Fight Academy,
              só são aplicados se estiverem tecnicamente configurados (por exemplo, com ID de medição)
              e após consentimento explícito do utilizador.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">5. Cookies de marketing</h2>
            <p className="mt-2">
              Atualmente, não são usados cookies de marketing próprios nesta plataforma, salvo futura
              configuração expressa e respetiva atualização desta política.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">6. Tabela de categorias de cookies</h2>
            <p className="mt-2">
              A tabela abaixo resume as categorias tratadas nesta política e o respetivo enquadramento
              de finalidade, consentimento e conservação.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">7. Preferências e gestão de cookies</h2>
            <p className="mt-2">
              Pode aceitar, rejeitar ou personalizar cookies opcionais no banner/centro de
              preferências, quando disponível.
            </p>
            <p className="mt-2">
              Também pode configurar o navegador para bloquear ou apagar cookies. O bloqueio de
              cookies estritamente necessários pode impedir login, sessão e funcionalidades da conta,
              incluindo áreas de aluno e administração.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">8. Serviços externos</h2>
            <p className="mt-2">
              Ao abrir serviços externos, como WhatsApp/Meta ou Stripe, esses fornecedores podem usar
              os seus próprios cookies nos respetivos ambientes, sob os termos e políticas deles.
            </p>
            <p className="mt-2">
              Se forem adicionados no futuro conteúdos incorporados de terceiros (por exemplo, vídeos
              embebidos), esta política poderá ser atualizada.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">9. Alterações a esta política</h2>
            <p className="mt-2">
              Esta política pode ser atualizada para refletir alterações legais, técnicas ou
              operacionais. A versão em vigor estará sempre disponível nesta página.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">10. Contacto</h2>
            <p className="mt-2">4Four Fight Academy</p>
            <p>Para questões sobre privacidade e cookies: 4fourfight@gmail.com</p>
          </section>
        </div>

        <div className="mt-8 overflow-x-auto rounded-lg border border-zinc-800">
          <table className="min-w-full divide-y divide-zinc-800 text-left text-sm text-zinc-200">
            <thead className="bg-zinc-950/80 text-xs uppercase tracking-wider text-zinc-400">
              <tr>
                <th className="px-4 py-3">Categoria</th>
                <th className="px-4 py-3">Finalidade</th>
                <th className="px-4 py-3">Exemplos</th>
                <th className="px-4 py-3">Consentimento</th>
                <th className="px-4 py-3">Conservação</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-zinc-800">
              <tr>
                <td className="px-4 py-4 font-medium text-white">Estritamente necessários</td>
                <td className="px-4 py-4">
                  Login, sessão, segurança e registo das preferências de cookies.
                </td>
                <td className="px-4 py-4">Cookies de sessão/autenticação e preferência de cookies.</td>
                <td className="px-4 py-4">Não, quando estritamente necessários ao serviço solicitado.</td>
                <td className="px-4 py-4">
                  Sessão e/ou até alteração das preferências, conforme o tipo de cookie.
                </td>
              </tr>
              <tr>
                <td className="px-4 py-4 font-medium text-white">Preferências</td>
                <td className="px-4 py-4">
                  Guardar opções de interface e preferências não essenciais, quando aplicável.
                </td>
                <td className="px-4 py-4">Definições opcionais do utilizador.</td>
                <td className="px-4 py-4">Sim, quando não forem estritamente necessários.</td>
                <td className="px-4 py-4">Até alteração/remoção da preferência.</td>
              </tr>
              <tr>
                <td className="px-4 py-4 font-medium text-white">Analíticos (se configurados)</td>
                <td className="px-4 py-4">Medição de utilização para melhoria do site.</td>
                <td className="px-4 py-4">Apenas se configurados tecnicamente no projeto.</td>
                <td className="px-4 py-4">Sim.</td>
                <td className="px-4 py-4">
                  Conforme configuração do serviço analítico, quando existir.
                </td>
              </tr>
              <tr>
                <td className="px-4 py-4 font-medium text-white">Marketing (não usado atualmente)</td>
                <td className="px-4 py-4">Publicidade/segmentação comportamental.</td>
                <td className="px-4 py-4">Não configurado atualmente na plataforma.</td>
                <td className="px-4 py-4">Seria necessário consentimento prévio.</td>
                <td className="px-4 py-4">Não aplicável no estado atual.</td>
              </tr>
            </tbody>
          </table>
        </div>
      </div>
    </main>
  );
}
