import { createFileRoute } from "@tanstack/react-router";

export const Route = createFileRoute("/termos-utilizacao")({
  component: TermosUtilizacaoPage,
});

function TermosUtilizacaoPage() {
  return (
    <main className="min-h-screen bg-background px-4 pb-16 pt-28 text-foreground sm:px-6 lg:px-8">
      <div className="mx-auto max-w-5xl">
        <h1 className="text-3xl font-semibold text-white sm:text-4xl">Termos de Utilização</h1>
        <p className="mt-4 text-sm leading-7 text-zinc-300">
          Última atualização: 27 de maio de 2026.
        </p>

        <div className="mt-8 space-y-8 text-sm leading-7 text-zinc-300">
          <section>
            <h2 className="text-xl font-semibold text-white">1. Objeto</h2>
            <p className="mt-2">
              Estes termos regulam a utilização do website e da plataforma da 4Four Fight Academy.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">2. Identificação e contacto</h2>
            <p className="mt-2">4Four Fight Academy</p>
            <p>Email: 4fourfight@gmail.com</p>
            <p className="mt-2">
              Caso a entidade jurídica oficial seja formalizada ou os respetivos dados sejam
              atualizados, esta página poderá ser revista para refletir essa informação.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">3. Acesso e conta de utilizador</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>O utilizador deve fornecer dados verdadeiros, atuais e completos.</li>
              <li>As credenciais de acesso são pessoais e intransmissíveis.</li>
              <li>
                O utilizador é responsável pela confidencialidade da palavra-passe e da sessão.
              </li>
              <li>
                A academia pode suspender ou limitar acessos em caso de uso indevido, fraude, risco
                de segurança ou violação destes termos.
              </li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">4. Pré-inscrição e pré-venda</h2>
            <p className="mt-2">
              O utilizador pode criar conta e manifestar interesse através de
              pré-inscrição/pré-venda. A pré-inscrição, por si só, não corresponde necessariamente a
              uma adesão paga ativa.
            </p>
            <p className="mt-2">
              Enquanto a ativação total de pagamentos online estiver em fase de transição, a
              conclusão da adesão pode ocorrer por contacto WhatsApp ou presencialmente na receção.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">5. Planos e adesões</h2>
            <p className="mt-2">
              Os planos, preços e condições podem ser apresentados no site para informação
              comercial. A disponibilidade final de cada adesão pode depender de confirmação de
              pagamento, validação administrativa e capacidade operacional da academia.
            </p>
            <p className="mt-2">
              A ativação de adesão segue o processo interno da 4Four Fight Academy, que pode incluir
              confirmação automática ou validação manual, consoante o estado do fluxo de pagamento.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">6. Pagamentos</h2>
            <p className="mt-2">
              Quando os pagamentos online estiverem ativos, o processamento é efetuado pela Stripe.
              A aplicação da academia não armazena dados completos de cartão bancário.
            </p>
            <p className="mt-2">
              A apresentação de uma página de sucesso no frontend não prova, por si só, a existência
              de adesão ativa: a confirmação depende do estado de pagamento, backend e validação
              administrativa aplicável.
            </p>
            <p className="mt-2">
              Enquanto a configuração live estiver pendente, podem existir fluxos de pagamento
              manual ou presencial na receção.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">7. Cancelamentos e reembolsos</h2>
            <p className="mt-2">
              Pedidos de cancelamento e eventuais reembolsos devem ser tratados pelos canais
              oficiais da academia (email/receção), de acordo com a lei aplicável e com a política
              comercial em vigor no momento do pedido.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">
              8. Área de aluno e área administrativa
            </h2>
            <p className="mt-2">
              O acesso a áreas reservadas depende do perfil, estado de conta e permissões
              atribuídas. Não é permitido tentar aceder a áreas sem autorização.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">
              9. Conteúdos e uploads de professores/admin/equipa
            </h2>
            <p className="mt-2">
              Determinadas funcionalidades de upload destinam-se a professores, administradores e
              equipa autorizada para fins pedagógicos, institucionais e de gestão interna.
            </p>
            <p className="mt-2">
              Os utilizadores apenas podem aceder aos conteúdos que lhes sejam disponibilizados de
              forma legítima no respetivo contexto de conta e permissões.
            </p>
            <p className="mt-2">
              Não é permitida a cópia, distribuição ou reutilização não autorizada de conteúdos,
              incluindo imagens, vídeos, documentos e materiais técnicos.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">10. Conduta do utilizador</h2>
            <ul className="mt-2 list-disc space-y-1 pl-5">
              <li>Não utilizar a plataforma para fraude, abuso, spam ou finalidades ilícitas.</li>
              <li>Não tentar ataques técnicos, scraping abusivo ou acesso não autorizado.</li>
              <li>Não contornar medidas de segurança, autenticação ou controlo de permissões.</li>
              <li>Não fazer engenharia reversa ou exploração indevida dos sistemas.</li>
              <li>Não usar formulários e contactos para comunicações enganosas ou maliciosas.</li>
            </ul>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">11. Disponibilidade da plataforma</h2>
            <p className="mt-2">
              A plataforma pode ficar temporariamente indisponível por manutenção, atualizações,
              incidentes técnicos, falhas de fornecedores de alojamento/infraestrutura ou serviços
              de terceiros.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">12. Propriedade intelectual</h2>
            <p className="mt-2">
              O website, marca, interface, textos, imagens, vídeos e demais conteúdos pertencem à
              4Four Fight Academy ou a entidades que autorizaram a sua utilização. Qualquer
              utilização não autorizada é proibida.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">13. Privacidade e dados pessoais</h2>
            <p className="mt-2">
              O tratamento de dados pessoais é realizado nos termos da Política de Privacidade,
              disponível na plataforma.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">14. Cookies</h2>
            <p className="mt-2">
              A utilização de cookies está descrita na Política de Cookies, incluindo preferências,
              categorias e formas de gestão pelo utilizador.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">15. Serviços e links externos</h2>
            <p className="mt-2">
              Serviços externos, como WhatsApp/Meta, Stripe ou outros que venham a ser integrados,
              funcionam segundo os seus próprios termos e políticas de privacidade.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">16. Alterações aos termos</h2>
            <p className="mt-2">
              Estes termos podem ser atualizados para refletir alterações legais, técnicas,
              operacionais ou comerciais. A versão em vigor estará sempre disponível nesta página.
            </p>
          </section>

          <section>
            <h2 className="text-xl font-semibold text-white">17. Contacto</h2>
            <p className="mt-2">4Four Fight Academy</p>
            <p>Email: 4fourfight@gmail.com</p>
            <p className="mt-2">
              Para questões sobre conta, adesões, cancelamentos ou pedidos relacionados com estes
              termos, utilize os contactos oficiais.
            </p>
          </section>
        </div>
      </div>
    </main>
  );
}
