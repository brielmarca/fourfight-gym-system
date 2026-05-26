import { Link, createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/politica-privacidade")({
  component: PoliticaPrivacidadePage,
});

function PoliticaPrivacidadePage() {
  return (
    <main className="min-h-screen bg-background px-4 pb-16 pt-28 text-foreground sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        <h1 className="text-3xl font-semibold text-white sm:text-4xl">Política de Privacidade</h1>
        <p className="mt-4 text-sm leading-7 text-zinc-300">
          Esta política descreve como a 4Four Fight Academy trata dados pessoais através do seu
          website/plataforma, incluindo páginas públicas, registo/login, planos, contacto, pedidos
          de aula experimental quando aplicável, área de aluno e operações administrativas.
        </p>

        <p className="mt-6 rounded-md border border-amber-600/50 bg-amber-950/30 p-4 text-sm text-amber-200">
          Esta política é uma base informativa e deve ser revista pelo responsável legal/proteção de
          dados da 4Four Fight Academy antes da entrada em produção.
        </p>

        <div className="mt-8 space-y-8 text-sm leading-7 text-zinc-300">
          <section>
            <h2 className="text-xl font-semibold text-white">1. Responsável pelo tratamento</h2>
            <p className="mt-2">4Four Fight Academy</p>
            <p>Contacto: 4fourfight@gmail.com</p>
            <p className="mt-2 text-zinc-400">
              TODO: confirmar entidade legal, morada oficial e contacto do responsável de proteção
              de dados, se aplicável.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">2. Dados pessoais que podemos recolher</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Identificação: nome.</li>
              <li>Contacto: email, telefone.</li>
              <li>Conta: credenciais protegidas, estado da conta, função/perfil.</li>
              <li>Inscrição/planos: plano escolhido, estado de pagamento/adesão.</li>
              <li>Treino/gestão: modalidade, horários, presença/aulas se aplicável.</li>
              <li>Comunicação: mensagens enviadas por formulários/contacto.</li>
              <li>Segurança: logs técnicos, endereço IP, user-agent, tentativas de login.</li>
              <li>Cookies/preferências: escolha de consentimento de cookies.</li>
            </ul>
            <p className="mt-3">
              Caso sejam recolhidos dados de saúde ou restrições médicas no futuro, será necessária
              base legal adequada e informação adicional específica.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">3. Finalidades do tratamento</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Gestão de conta de utilizador e adesão de membro.</li>
              <li>Gestão operacional da academia e acompanhamento de alunos.</li>
              <li>Resposta a pedidos de contacto e suporte.</li>
              <li>Criar e gerir contas de utilizador.</li>
              <li>Permitir login e autenticação segura.</li>
              <li>Gerir planos, inscrições e pagamentos.</li>
              <li>Gerir horários, aulas e modalidades.</li>
              <li>Responder a contactos/pedidos.</li>
              <li>Garantir segurança da plataforma e prevenir abuso.</li>
              <li>Cumprir obrigações legais.</li>
              <li>Melhorar o site com analytics apenas se o utilizador consentir.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">4. Base legal</h2>
            <div className="mt-3 overflow-x-auto rounded-lg border border-zinc-800">
              <table className="min-w-full divide-y divide-zinc-800 text-left text-sm text-zinc-200">
                <thead className="bg-zinc-950/80 text-xs uppercase tracking-wider text-zinc-400">
                  <tr>
                    <th className="px-4 py-3">Finalidade</th>
                    <th className="px-4 py-3">Base legal</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-zinc-800">
                  <tr>
                    <td className="px-4 py-4">Gestão de conta e prestação do serviço</td>
                    <td className="px-4 py-4">Execução de contrato/diligências pré-contratuais</td>
                  </tr>
                  <tr>
                    <td className="px-4 py-4">Pagamentos e faturação</td>
                    <td className="px-4 py-4">Execução de contrato e obrigação legal</td>
                  </tr>
                  <tr>
                    <td className="px-4 py-4">Segurança, logs e prevenção de abuso</td>
                    <td className="px-4 py-4">Interesse legítimo e segurança da plataforma</td>
                  </tr>
                  <tr>
                    <td className="px-4 py-4">Resposta a contactos</td>
                    <td className="px-4 py-4">Diligências pré-contratuais ou interesse legítimo</td>
                  </tr>
                  <tr>
                    <td className="px-4 py-4">Comunicações de marketing</td>
                    <td className="px-4 py-4">Consentimento, quando aplicável</td>
                  </tr>
                  <tr>
                    <td className="px-4 py-4">Analytics/cookies não necessários</td>
                    <td className="px-4 py-4">Consentimento</td>
                  </tr>
                </tbody>
              </table>
            </div>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">5. Conservação dos dados</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Dados de conta: enquanto a conta estiver ativa ou necessário para obrigações legais.</li>
              <li>Dados de pagamento/faturação: pelo período exigido por lei.</li>
              <li>Contactos/formulários: pelo tempo necessário para responder e gerir o pedido.</li>
              <li>Logs de segurança: por período limitado e proporcional.</li>
              <li>
                Consentimento de cookies: até alteração das preferências ou expiração definida.
              </li>
            </ul>
            <p className="mt-2 text-zinc-400">
              TODO: Confirmar prazos concretos de retenção com o responsável legal/contabilístico.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">6. Partilha de dados com terceiros</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Alojamento/hosting.</li>
              <li>Base de dados/infraestrutura.</li>
              <li>Processamento de pagamentos, se aplicável.</li>
              <li>Email/transacional, se aplicável.</li>
              <li>Analytics apenas com consentimento.</li>
              <li>Autoridades quando exigido por lei.</li>
            </ul>
            <p className="mt-3">Os pagamentos são processados pela Stripe.</p>
            <p className="mt-1">A aplicação não armazena dados completos de cartão bancário.</p>
            <p className="mt-3">Os dados não são vendidos.</p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">7. Transferências internacionais</h2>
            <p className="mt-2">
              Alguns fornecedores tecnológicos podem tratar dados fora do Espaço Económico Europeu
              (EEE). Nesses casos, devem ser confirmadas garantias adequadas para a transferência de
              dados pessoais.
            </p>
            <p className="mt-2 text-zinc-400">
              TODO: Confirmar fornecedores reais e mecanismos de transferência antes de produção.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">8. Direitos dos utilizadores</h2>
            <p className="mt-2">Nos termos aplicáveis, o utilizador pode exercer os direitos de:</p>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Acesso.</li>
              <li>Retificação.</li>
              <li>Apagamento.</li>
              <li>Limitação do tratamento.</li>
              <li>Oposição.</li>
              <li>Portabilidade.</li>
              <li>Retirar consentimento.</li>
              <li>Apresentar reclamação à CNPD.</li>
            </ul>
            <p className="mt-3">
              Os pedidos podem ser enviados para 4fourfight@gmail.com, sujeitos a verificação de
              identidade.
            </p>
            <p className="mt-2">Também pode pedir a correção ou eliminação dos seus dados por este contacto.</p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">9. Segurança</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Autenticação.</li>
              <li>Passwords hashed.</li>
              <li>HTTPS/TLS em produção.</li>
              <li>Controlo de acesso.</li>
              <li>Proteção contra abuso/rate limiting.</li>
              <li>Logs de segurança proporcionais.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">10. Cookies</h2>
            <p className="mt-2">
              Para mais informação sobre cookies e preferências, consulta a{" "}
              <Link to="/politica-cookies" className="text-red-400 underline underline-offset-4 hover:text-red-300">
                Política de Cookies
              </Link>
              .
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">11. Alterações à política</h2>
            <p className="mt-2">
              Esta política pode ser atualizada periodicamente. A versão mais recente será publicada no
              site.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">12. Contacto</h2>
            <p className="mt-2">4fourfight@gmail.com</p>
            <p className="mt-2 text-zinc-400">
              TODO: Confirmar dados oficiais da entidade legal e contacto institucional.
            </p>
          </section>
        </div>
      </div>
    </main>
  );
}
