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
          Esta política descreve como a 4Four Fight Academy trata dados pessoais no website e na
          plataforma, em conformidade com o RGPD e a legislação aplicável em Portugal e na União
          Europeia.
        </p>

        <div className="mt-8 space-y-8 text-sm leading-7 text-zinc-300">
          <section>
            <h2 className="text-xl font-semibold text-white">1. Última atualização</h2>
            <p className="mt-2">27 de maio de 2026.</p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">2. Âmbito e introdução</h2>
            <p className="mt-2">
              Esta política aplica-se às páginas públicas do site, registo/login, fluxos de
              pré-inscrição e pré-venda, planos/adesões, formulário de contacto, links para
              WhatsApp, área de aluno e área administrativa.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">3. Responsável e contacto</h2>
            <p className="mt-2">4Four Fight Academy</p>
            <p>Email: 4fourfight@gmail.com</p>
            <p className="mt-2">
              Caso os dados da entidade jurídica oficial sejam formalizados ou atualizados, esta
              política poderá ser revista para refletir essa informação.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">4. Dados pessoais tratados</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Identificação e contacto: nome, email, telefone e conteúdo de mensagens.</li>
              <li>
                Conta: email de login, palavra-passe cifrada por hash, estado da conta e
                perfil/role.
              </li>
              <li>Pré-inscrição, pré-venda, planos e adesões.</li>
              <li>Estado de pagamento e referências técnicas associadas ao fluxo de pagamento.</li>
              <li>Dados de treino e gestão do ginásio, quando aplicável ao serviço.</li>
              <li>
                Logs técnicos e de segurança: IP, user-agent, tentativas de autenticação e eventos
                de segurança.
              </li>
              <li>
                Uploads por administradores/professores/equipa autorizada: imagens, vídeos,
                documentos e ficheiros, incluindo metadados (nome, tipo, tamanho, data e utilizador
                que carregou).
              </li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">
              5. Dados sensíveis e informação de saúde
            </h2>
            <p className="mt-2">
              Sempre que sejam necessários dados de saúde, limitações médicas ou contactos de
              emergência para segurança e gestão do treino, esses dados devem ser tratados com
              cuidado reforçado, apenas na medida do necessário e com base jurídica adequada.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">6. Finalidades do tratamento</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Criar e gerir contas, login e autenticação.</li>
              <li>Responder a pedidos de pré-inscrição, pré-venda e contacto.</li>
              <li>Gerir planos, adesões/mensalidades e respetivos estados.</li>
              <li>Suportar a gestão da área de aluno e da área administrativa.</li>
              <li>Permitir contacto via WhatsApp quando o utilizador seleciona essa opção.</li>
              <li>Prevenir fraude, abuso e incidentes de segurança.</li>
              <li>Cumprir obrigações legais, fiscais e contabilísticas.</li>
              <li>
                Gerir conteúdos carregados por equipa autorizada para finalidades pedagógicas e
                operacionais.
              </li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">7. Bases jurídicas</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Execução de contrato e diligências pré-contratuais.</li>
              <li>Cumprimento de obrigações legais.</li>
              <li>Interesse legítimo, nomeadamente segurança e gestão operacional.</li>
              <li>Consentimento, quando legalmente exigido.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">8. Pagamentos</h2>
            <p className="mt-2">
              Quando ativados, os pagamentos online são processados pela Stripe. A aplicação da
              4Four Fight Academy não armazena dados completos de cartão bancário.
            </p>
            <p className="mt-2">
              Enquanto a ativação pública do pagamento online estiver em transição, poderão ser
              usados fluxos de pré-venda, contacto por WhatsApp ou tratamento presencial na receção.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">9. WhatsApp</h2>
            <p className="mt-2">
              Ao clicar num link de WhatsApp, o utilizador é encaminhado para um ambiente externo da
              WhatsApp/Meta. O tratamento realizado nesses serviços segue os termos e políticas de
              privacidade da respetiva plataforma.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">
              10. Uploads por professores/admin/equipa autorizada
            </h2>
            <p className="mt-2">
              As funcionalidades de upload destinam-se a administradores, professores e membros da
              equipa autorizados. Por omissão, alunos/clientes não carregam ficheiros na plataforma.
            </p>
            <p className="mt-2">
              Os conteúdos podem incluir imagens, vídeos, documentos e outros ficheiros relacionados
              com aulas, movimentos técnicos, modalidades, comunicação institucional e gestão
              interna do ginásio.
            </p>
            <p className="mt-2">
              Sempre que um conteúdo inclua pessoas identificáveis, deve ser tratado como dado
              pessoal e carregado apenas com finalidade válida e autorização/base jurídica adequada.
            </p>
            <p className="mt-2">
              Se forem adicionadas no futuro funcionalidades de upload para alunos/clientes, esta
              política será atualizada.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">11. Fornecedores e terceiros</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Cloudflare Pages (alojamento do frontend).</li>
              <li>Render (alojamento do backend).</li>
              <li>Supabase/PostgreSQL (base de dados e infraestrutura).</li>
              <li>Stripe (processamento de pagamentos, quando ativo).</li>
              <li>WhatsApp/Meta (contacto externo acionado pelo utilizador).</li>
              <li>Fornecedores de email transacional, apenas se/quando implementados.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">12. Transferências internacionais</h2>
            <p className="mt-2">
              Alguns fornecedores tecnológicos podem tratar dados fora do Espaço Económico Europeu.
              Quando aplicável, devem ser adotadas as salvaguardas exigidas pela legislação em
              vigor.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">13. Conservação dos dados</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>
                Dados de conta: durante a relação ativa e pelo período necessário às obrigações
                legais.
              </li>
              <li>
                Pré-inscrição e contacto: pelo tempo necessário à gestão do pedido e seguimento
                comercial.
              </li>
              <li>Pagamentos e dados fiscais/contabilísticos: pelos prazos legalmente exigidos.</li>
              <li>
                Logs e eventos de segurança: por período limitado e proporcional às finalidades de
                segurança.
              </li>
              <li>
                Conteúdos carregados por equipa autorizada: enquanto forem úteis/necessários a fins
                pedagógicos e administrativos, ou até remoção/pedido aplicável.
              </li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">14. Segurança</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Ligações seguras HTTPS/TLS em produção.</li>
              <li>Palavras-passe protegidas por hash no backend.</li>
              <li>Controlo de acesso por perfil/função (RBAC).</li>
              <li>Modelo de sessão/autenticação com cookie seguro para refresh token.</li>
              <li>Medidas de mitigação de abuso, incluindo rate limiting.</li>
              <li>Separação funcional entre áreas administrativas e de aluno.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">
              15. Direitos dos titulares dos dados
            </h2>
            <p className="mt-2">Nos termos legais, pode exercer os seguintes direitos:</p>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Acesso.</li>
              <li>Retificação.</li>
              <li>Apagamento.</li>
              <li>Limitação do tratamento.</li>
              <li>Oposição.</li>
              <li>Portabilidade.</li>
              <li>Retirada do consentimento (quando aplicável).</li>
              <li>Reclamação junto da CNPD.</li>
            </ul>
            <p className="mt-3">
              Para exercer direitos, contacte 4fourfight@gmail.com. Pode ser solicitada verificação
              de identidade para proteção dos dados pessoais.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">16. Cookies</h2>
            <p className="mt-2">
              Para informação detalhada sobre categorias de cookies e gestão de preferências,
              consulte a{" "}
              <Link
                to="/politica-cookies"
                className="text-red-400 underline underline-offset-4 hover:text-red-300"
              >
                Política de Cookies
              </Link>
              .
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">17. Alterações a esta política</h2>
            <p className="mt-2">
              Esta política pode ser atualizada para refletir alterações legais, técnicas ou
              operacionais. A versão mais recente estará sempre disponível nesta página.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">18. Contacto</h2>
            <p className="mt-2">4Four Fight Academy</p>
            <p>Email: 4fourfight@gmail.com</p>
          </section>
        </div>
      </div>
    </main>
  );
}
