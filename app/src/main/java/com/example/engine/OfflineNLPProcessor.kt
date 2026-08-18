package com.example.engine

import com.example.data.local.ActivityEntity
import com.example.data.local.ProtocolEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.random.Random

sealed class ProcessedResult {
    data class CreateActivity(
        val activity: ActivityEntity,
        val responseMessage: String
    ) : ProcessedResult()

    data class CompleteActivity(
        val targetQuery: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class DeleteActivity(
        val targetQuery: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class UpdatePriority(
        val targetQuery: String,
        val newPriority: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class PostponeActivity(
        val targetQuery: String,
        val newDueDate: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class ClearCompleted(
        val responseMessage: String
    ) : ProcessedResult()

    data class RunProtocol(
        val protocolId: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class StartTimer(
        val minutes: Int,
        val title: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class PauseTimer(
        val responseMessage: String
    ) : ProcessedResult()

    data class ResumeTimer(
        val responseMessage: String
    ) : ProcessedResult()

    data class ResetTimer(
        val responseMessage: String
    ) : ProcessedResult()

    data class Calculate(
        val expression: String,
        val result: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class UnitConversion(
        val fromValue: Double,
        val fromUnit: String,
        val toValue: String,
        val toUnit: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class SystemStatus(
        val responseMessage: String
    ) : ProcessedResult()

    data class DailyBriefing(
        val responseMessage: String
    ) : ProcessedResult()

    data class DatabaseStats(
        val responseMessage: String
    ) : ProcessedResult()

    data class SaveNote(
        val noteContent: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class ViewNotes(
        val responseMessage: String
    ) : ProcessedResult()

    data class ClearNotes(
        val responseMessage: String
    ) : ProcessedResult()

    data class ToggleVoice(
        val enable: Boolean,
        val responseMessage: String
    ) : ProcessedResult()

    data class ToggleMode(
        val online: Boolean,
        val responseMessage: String
    ) : ProcessedResult()

    data class RandomNumber(
        val min: Int,
        val max: Int,
        val result: Int,
        val responseMessage: String
    ) : ProcessedResult()

    data class CoinFlip(
        val result: String,
        val responseMessage: String
    ) : ProcessedResult()

    data class ShowCommandManual(
        val category: String? = null,
        val responseMessage: String
    ) : ProcessedResult()

    data class ConversationalResponse(
        val responseMessage: String,
        val actionTag: String? = null
    ) : ProcessedResult()
}

/**
 * CommandDefinition for UI catalog and voice help
 */
data class OfflineCommandInfo(
    val id: String,
    val category: String,
    val pattern: String,
    val example: String,
    val description: String
)

class OfflineNLPProcessor {

    // Quick in-memory scratchpad notes
    private val quickNotes = mutableListOf<String>()

    /**
     * Catalog of 44+ Offline Commands for quick display and discoverability
     */
    val commandCatalog: List<OfflineCommandInfo> = listOf(
        // Categoria 1: Atividades & SQLite (1-8)
        OfflineCommandInfo("cmd_create_task", "Atividades", "criar tarefa [nome] [prioridade] [prazo] [tempo]", "Criar tarefa Revisar código urgente amanhã 45m", "Cria e salva nova atividade no banco SQLite com prioridade e prazo."),
        OfflineCommandInfo("cmd_complete_task", "Atividades", "concluir tarefa [nome/id]", "Concluir tarefa Revisar código", "Marca a atividade correspondente como concluída."),
        OfflineCommandInfo("cmd_list_pending", "Atividades", "listar pendentes / minhas tarefas", "Listar pendentes", "Exibe todas as tarefas ativas ordenadas por prioridade."),
        OfflineCommandInfo("cmd_list_completed", "Atividades", "listar concluidas / tarefas feitas", "Listar concluidas", "Exibe histórico de tarefas já finalizadas."),
        OfflineCommandInfo("cmd_delete_task", "Atividades", "excluir tarefa [nome/id]", "Excluir tarefa #1", "Remove permanentemente a atividade selecionada do SQLite."),
        OfflineCommandInfo("cmd_clear_completed", "Atividades", "limpar concluidas / expurgar concluidas", "Limpar concluidas", "Limpa todas as atividades finalizadas do banco de dados."),
        OfflineCommandInfo("cmd_postpone_task", "Atividades", "adiar tarefa [nome] para [prazo]", "Adiar tarefa Relatório para amanhã", "Atualiza a data limite de entrega da atividade."),
        OfflineCommandInfo("cmd_change_priority", "Atividades", "definir prioridade [alta/média/baixa] em [nome]", "Definir prioridade alta em Estudo", "Atualiza o nível de criticidade da tarefa."),

        // Categoria 2: Protocolos Operacionais (9-14)
        OfflineCommandInfo("cmd_proto_focus", "Protocolos", "protocolo foco / iniciar foco", "Protocolo foco", "Inicia sessão Pomodoro de 25m e aloca tarefas de foco no SQLite."),
        OfflineCommandInfo("cmd_proto_morning", "Protocolos", "protocolo matinal / iniciar dia", "Protocolo matinal", "Carrega diretrizes de alinhamento e rotina matinal."),
        OfflineCommandInfo("cmd_proto_deep", "Protocolos", "protocolo deep work / foco profundo", "Protocolo deep work", "Ativa imersão de 50 minutos para trabalho de alta densidade."),
        OfflineCommandInfo("cmd_proto_evening", "Protocolos", "protocolo noturno / encerrar dia", "Protocolo noturno", "Inicia rotina de desaceleração e balanço do dia."),
        OfflineCommandInfo("cmd_proto_break", "Protocolos", "protocolo descompressao / pausa tatica", "Protocolo descompressão", "Temporizador de 10 minutos para descanso neural."),
        OfflineCommandInfo("cmd_proto_security", "Protocolos", "protocolo seguranca / checar integridade", "Protocolo segurança", "Verifica permissões, integridade das tabelas e logs."),

        // Categoria 3: Temporizadores & Foco (15-18)
        OfflineCommandInfo("cmd_timer_start", "Temporizadores", "iniciar timer [X] minutos / temporizador [X]m", "Iniciar timer 30 minutos", "Inicia contagem regressiva personalizada."),
        OfflineCommandInfo("cmd_timer_pause", "Temporizadores", "pausar timer / pausar foco", "Pausar timer", "Interrompe temporariamente a contagem atual."),
        OfflineCommandInfo("cmd_timer_resume", "Temporizadores", "continuar timer / retomar foco", "Continuar timer", "Retoma a contagem regressiva em andamento."),
        OfflineCommandInfo("cmd_timer_reset", "Temporizadores", "cancelar timer / resetar timer", "Resetar timer", "Zera e cancela a sessão de temporizador ativa."),

        // Categoria 4: Matemática & Porcentagens (19-20)
        OfflineCommandInfo("cmd_math_calc", "Cálculos", "calcular [expressão] / quanto é [conta]", "Calcular 1250 * 0.18 + 45", "Avalia expressões aritméticas (+, -, *, /, %, ^) localmente."),
        OfflineCommandInfo("cmd_math_percent", "Cálculos", "porcentagem [X]% de [Y]", "Porcentagem 15% de 800", "Calcula porcentagem instantânea."),

        // Categoria 5: Conversão de Unidades (21-24)
        OfflineCommandInfo("cmd_conv_dist", "Conversões", "converter [X] km para milhas / metros", "Converter 10 km para milhas", "Converte distâncias métricas e imperiais."),
        OfflineCommandInfo("cmd_conv_weight", "Conversões", "converter [X] kg para libras / gramas", "Converter 75 kg para libras", "Converte unidades de peso e massa."),
        OfflineCommandInfo("cmd_conv_temp", "Conversões", "converter [X] celsius para fahrenheit", "Converter 25 celsius para fahrenheit", "Converte escalas de temperatura."),
        OfflineCommandInfo("cmd_conv_time", "Conversões", "converter [X] horas para minutos / segundos", "Converter 3.5 horas para minutos", "Converte grandezas de tempo."),

        // Categoria 6: Telemetria & SQLite (25-29)
        OfflineCommandInfo("cmd_status_sys", "Telemetria", "status do sistema / diagnostico", "Status do sistema", "Relatório de integridade do SQLite, memória e tarefas."),
        OfflineCommandInfo("cmd_briefing_day", "Telemetria", "briefing diario / resumo de hoje", "Briefing diário", "Resumo das principais pendências e metas do dia."),
        OfflineCommandInfo("cmd_db_stats", "Telemetria", "estatisticas de banco / memoria sqlite", "Estatísticas de banco", "Exibe contadores de linhas, logs e atividades armazenadas."),
        OfflineCommandInfo("cmd_view_logs", "Telemetria", "ver logs / historico de comandos", "Ver logs", "Lista as últimas instruções executadas pelo assistente."),
        OfflineCommandInfo("cmd_clear_logs", "Telemetria", "limpar logs / limpar terminal", "Limpar logs", "Esvazia o histórico holográfico de interações."),

        // Categoria 7: Notas Rápidas / Scratchpad (30-33)
        OfflineCommandInfo("cmd_note_add", "Notas", "anotar [texto] / nota rapida [texto]", "Anotar reunião com equipe técnica às 16h", "Armazena nota no bloco de notas rápido."),
        OfflineCommandInfo("cmd_note_view", "Notas", "ler notas / ver anotacoes", "Ler notas", "Exibe todas as notas temporárias salvas na sessão."),
        OfflineCommandInfo("cmd_note_clear", "Notas", "limpar notas / esvaziar notas", "Limpar notas", "Remove todas as anotações do scratchpad."),
        OfflineCommandInfo("cmd_copy_last", "Notas", "repetir ultimo comando", "Repetir último comando", "Reexecuta ou exibe a última diretriz recebida."),

        // Categoria 8: Controle de Voz & Síntese (34-36)
        OfflineCommandInfo("cmd_voice_mute", "Voz & Áudio", "silenciar voz / mutar assistente", "Silenciar voz", "Desativa o sintetizador de fala (Text-to-Speech)."),
        OfflineCommandInfo("cmd_voice_unmute", "Voz & Áudio", "ativar voz / desmutar assistente", "Ativar voz", "Ativa o sintetizador de voz Jo'On."),
        OfflineCommandInfo("cmd_voice_test", "Voz & Áudio", "testar voz / sintetizar teste", "Testar voz", "Executa frase de calibração sonora."),

        // Categoria 9: Utilitários & Random (37-40)
        OfflineCommandInfo("cmd_util_time", "Utilitários", "que horas sao / data de hoje", "Que horas são", "Informa horário preciso e data do sistema."),
        OfflineCommandInfo("cmd_util_random", "Utilitários", "sortear numero de [X] a [Y]", "Sortear numero de 1 a 100", "Gera número pseudo-aleatório no intervalo."),
        OfflineCommandInfo("cmd_util_coin", "Utilitários", "cara ou coroa / jogar moeda", "Cara ou coroa", "Sorteio de probabilidade binária com feedback sonoro."),
        OfflineCommandInfo("cmd_util_online", "Utilitários", "ativar modo online / conectar gemini", "Ativar modo online", "Alterna o canal para o modelo Gemini 3.5 Flash."),

        // Categoria 10: Inteligência & Ajuda (41-44)
        OfflineCommandInfo("cmd_util_offline", "Inteligência", "ativar modo offline / modo local", "Ativar modo offline", "Retorna ao núcleo local de processamento SQLite."),
        OfflineCommandInfo("cmd_intel_quote", "Inteligência", "frase motivacional / citacao operacional", "Frase motivacional", "Disponibiliza citação de alta performance e foco."),
        OfflineCommandInfo("cmd_intel_tip", "Inteligência", "dica de produtividade / conselho tatico", "Dica de produtividade", "Instrução tática de gestão de energia e foco."),
        OfflineCommandInfo("cmd_intel_help", "Inteligência", "manual de comandos / ajuda / listar comandos", "Manual de comandos", "Abre a lista com os 40+ comandos offline suportados.")
    )

    fun processCommand(
        input: String,
        currentActivities: List<ActivityEntity>,
        protocols: List<ProtocolEntity>
    ): ProcessedResult {
        val clean = input.trim().lowercase(Locale.ROOT)
            .replace("jo'on", "")
            .replace("joon", "")
            .replace("jarvis", "")
            .trim()

        // -------------------------------------------------------------
        // 1. MATH & CALCULATIONS (Commands 19, 20)
        // -------------------------------------------------------------
        if (clean.startsWith("porcentagem") || clean.contains("% de")) {
            val match = Regex("(\\d+(?:[.,]\\d+)?)\\s*%\\s*de\\s*(\\d+(?:[.,]\\d+)?)").find(clean)
            if (match != null) {
                val p = match.groupValues[1].replace(",", ".").toDoubleOrNull() ?: 0.0
                val total = match.groupValues[2].replace(",", ".").toDoubleOrNull() ?: 0.0
                val result = (p / 100.0) * total
                val formatted = if (result % 1.0 == 0.0) result.toLong().toString() else String.format(Locale.US, "%.2f", result)
                return ProcessedResult.Calculate(
                    expression = "$p% de $total",
                    result = formatted,
                    responseMessage = "Cálculo percentual: $p% de $total = $formatted"
                )
            }
        }

        if (clean.startsWith("calcular") || clean.startsWith("quanto é") || clean.startsWith("calcula") || clean.matches(Regex("^[0-9+\\-*/().^% ]+$"))) {
            val expr = clean.replace("calcular", "").replace("quanto é", "").replace("calcula", "").trim()
            val mathResult = evalMath(expr)
            return if (mathResult != null) {
                ProcessedResult.Calculate(
                    expression = expr,
                    result = mathResult,
                    responseMessage = "Cálculo computado no núcleo local: $expr = $mathResult"
                )
            } else {
                ProcessedResult.ConversationalResponse("Não foi possível processar a expressão matemática '$expr'. Verifique os operadores.")
            }
        }

        // -------------------------------------------------------------
        // 2. UNIT CONVERSIONS (Commands 21, 22, 23, 24)
        // -------------------------------------------------------------
        if (clean.startsWith("converter") || clean.startsWith("converte") || clean.startsWith("transformar")) {
            val convResult = handleUnitConversion(clean)
            if (convResult != null) return convResult
        }

        // -------------------------------------------------------------
        // 3. TIME & DATE (Command 37)
        // -------------------------------------------------------------
        if (clean.contains("que horas") || clean.contains("hora atual") || clean.contains("que dia") || clean.contains("data de hoje") || clean.contains("data atual")) {
            val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
            val date = SimpleDateFormat("dd 'de' MMMM 'de' yyyy", Locale("pt", "BR")).format(Date())
            return ProcessedResult.ConversationalResponse(
                "Horário sincronizado: $time. Data do sistema: $date.",
                "TIME_DATE"
            )
        }

        // -------------------------------------------------------------
        // 4. RANDOM / COIN FLIP (Commands 38, 39)
        // -------------------------------------------------------------
        if (clean.contains("cara ou coroa") || clean.contains("jogar moeda") || clean.contains("moeda")) {
            val outcome = if (Random.nextBoolean()) "CARA" else "COROA"
            return ProcessedResult.CoinFlip(
                result = outcome,
                responseMessage = "Lançamento de probabilidade binária realizado. Resultado: $outcome."
            )
        }

        if (clean.startsWith("sortear") || clean.contains("numero aleatorio") || clean.contains("gerar numero")) {
            val match = Regex("(\\d+)\\s*(?:a|ate|e)\\s*(\\d+)").find(clean)
            val (min, max) = if (match != null) {
                val n1 = match.groupValues[1].toIntOrNull() ?: 1
                val n2 = match.groupValues[2].toIntOrNull() ?: 100
                if (n1 <= n2) n1 to n2 else n2 to n1
            } else {
                1 to 100
            }
            val rolled = Random.nextInt(min, max + 1)
            return ProcessedResult.RandomNumber(
                min = min,
                max = max,
                result = rolled,
                responseMessage = "Número aleatório sorteado no intervalo [$min - $max]: $rolled"
            )
        }

        // -------------------------------------------------------------
        // 5. MOTIVATIONAL & TIPS (Commands 41, 42)
        // -------------------------------------------------------------
        if (clean.contains("motivacional") || clean.contains("frase") || clean.contains("inspiracao") || clean.contains("citacao")) {
            val quotes = listOf(
                "\"Disciplina é a ponte entre metas e conquistas.\" - Jim Rohn",
                "\"A excelência não é um ato, mas um hábito.\" - Aristóteles",
                "\"Foque no que você pode controlar. O resto é ruído estático.\"",
                "\"Ação rápida e precisão cirúrgica superam a hesitação.\" - Protocolo Jo'On",
                "\"Grandes impérios são construídos um bloco por vez, com consistência inabalável.\""
            )
            val quote = quotes.random()
            return ProcessedResult.ConversationalResponse(quote, "QUOTE")
        }

        if (clean.contains("dica") || clean.contains("produtividade") || clean.contains("conselho")) {
            val tips = listOf(
                "Dica Tática: Agrupe tarefas semelhantes em blocos de foco (Batching) para evitar trocas constantes de contexto.",
                "Dica Tática: Aplique a regra dos 2 minutos — se uma tarefa leva menos de 2 minutos, execute-a imediatamente.",
                "Dica Tática: Defina no máximo 3 atividades de prioridade ALTA por dia. Conclua a mais difícil primeiro.",
                "Dica Tática: Utilize sessões Pomodoro de 25 ou 50 minutos com intervalos curtos de descompressão."
            )
            return ProcessedResult.ConversationalResponse(tips.random(), "TIP")
        }

        // -------------------------------------------------------------
        // 6. SCRATCHPAD / QUICK NOTES (Commands 30, 31, 32)
        // -------------------------------------------------------------
        if (clean.startsWith("anotar") || clean.startsWith("nota rapida") || clean.startsWith("lembrete rapido") || clean.startsWith("nota:")) {
            val note = clean
                .replace(Regex("^(anotar|nota rapida|lembrete rapido|nota:?)"), "")
                .trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }

            if (note.isNotEmpty()) {
                quickNotes.add(note)
                return ProcessedResult.SaveNote(
                    noteContent = note,
                    responseMessage = "Nota anotada no scratchpad:\n\"$note\""
                )
            }
        }

        if (clean.contains("ler notas") || clean.contains("ver notas") || clean.contains("ver anotacoes") || clean.contains("minhas notas")) {
            return if (quickNotes.isEmpty()) {
                ProcessedResult.ViewNotes("Scratchpad vazio. Use 'anotar [texto]' para registrar pensamentos rápidos.")
            } else {
                val formatted = quickNotes.mapIndexed { idx, n -> "${idx + 1}. $n" }.joinToString("\n")
                ProcessedResult.ViewNotes("Anotações salvas nesta sessão (${quickNotes.size}):\n$formatted")
            }
        }

        if (clean.contains("limpar notas") || clean.contains("esvaziar notas") || clean.contains("apagar notas")) {
            quickNotes.clear()
            return ProcessedResult.ClearNotes("Todas as notas rápidas foram expurgadas da memória de trabalho.")
        }

        // -------------------------------------------------------------
        // 7. VOICE & AUDIO CONTROLS (Commands 34, 35, 36)
        // -------------------------------------------------------------
        if (clean.contains("silenciar voz") || clean.contains("mutar") || clean.contains("desativar voz") || clean.contains("modo silencioso")) {
            return ProcessedResult.ToggleVoice(false, "Voz do assistente desativada. As respostas serão apenas visuais.")
        }

        if (clean.contains("ativar voz") || clean.contains("desmutar") || clean.contains("ligar voz")) {
            return ProcessedResult.ToggleVoice(true, "Voz do assistente ativada. Síntese neural restabelecida.")
        }

        if (clean.contains("testar voz") || clean.contains("calibrar som")) {
            return ProcessedResult.ConversationalResponse(
                "Teste de síntese vocal Jo'On nominal. Todos os sistemas de áudio operando em alta fidelidade.",
                "VOICE_TEST"
            )
        }

        // -------------------------------------------------------------
        // 8. ONLINE / OFFLINE SWITCH (Commands 37, 38)
        // -------------------------------------------------------------
        if (clean.contains("ativar modo online") || clean.contains("conectar gemini") || clean.contains("modo online") || clean.contains("ligar nuvem")) {
            return ProcessedResult.ToggleMode(true, "Modo Online ativado. Conexão neural com Gemini estabelecida.")
        }

        if (clean.contains("ativar modo offline") || clean.contains("modo local") || clean.contains("desconectar nuvem")) {
            return ProcessedResult.ToggleMode(false, "Modo Offline ativado. Processamento restrito ao SQLite local.")
        }

        // -------------------------------------------------------------
        // 9. TIMER & POMODORO (Commands 15, 16, 17, 18)
        // -------------------------------------------------------------
        if (clean.startsWith("iniciar timer") || clean.startsWith("temporizador") || clean.startsWith("timer") || clean.contains("iniciar cronometro")) {
            val minsMatch = Regex("(\\d+)\\s*(?:m|min|minutos)?").find(clean)
            val mins = minsMatch?.groupValues?.get(1)?.toIntOrNull() ?: 25
            return ProcessedResult.StartTimer(
                minutes = mins,
                title = "Temporizador de $mins min",
                responseMessage = "Temporizador de $mins minutos iniciado. Mantenha o foco absoluto."
            )
        }

        if (clean.contains("pausar timer") || clean.contains("pausar foco") || clean.contains("congelar timer")) {
            return ProcessedResult.PauseTimer("Temporizador pausado.")
        }

        if (clean.contains("continuar timer") || clean.contains("retomar timer") || clean.contains("retomar foco") || clean.contains("despausar")) {
            return ProcessedResult.ResumeTimer("Temporizador retomado.")
        }

        if (clean.contains("cancelar timer") || clean.contains("resetar timer") || clean.contains("parar timer") || clean.contains("zerar timer")) {
            return ProcessedResult.ResetTimer("Temporizador cancelado e resetado.")
        }

        // -------------------------------------------------------------
        // 10. PROTOCOLS (Commands 9-14)
        // -------------------------------------------------------------
        if (clean.contains("protocolo") || clean.contains("iniciar foco") || clean.contains("modo foco") || clean.contains("rotina")) {
            val matchingProtocol = protocols.find { protocol ->
                clean.contains(protocol.name.lowercase(Locale.ROOT)) ||
                (clean.contains("foco") && protocol.id.contains("FOCUS")) ||
                (clean.contains("matinal") || clean.contains("manhã") || clean.contains("dia")) && protocol.id.contains("BRIEFING") ||
                (clean.contains("deep") || clean.contains("profundo")) && protocol.id.contains("DEEP") ||
                (clean.contains("noturno") || clean.contains("noite")) && protocol.id.contains("EVENING") ||
                (clean.contains("descompress") || clean.contains("pausa") || clean.contains("descanso")) && protocol.id.contains("BREAK") ||
                (clean.contains("segurança") || clean.contains("backup")) && protocol.id.contains("SECURITY")
            }

            if (matchingProtocol != null) {
                val steps = matchingProtocol.stepsList.split("|").joinToString("\n• ", prefix = "• ")
                return ProcessedResult.RunProtocol(
                    protocolId = matchingProtocol.id,
                    responseMessage = "Protocolo [${matchingProtocol.name}] ativado.\nDiretrizes operacionais alocadas:\n$steps"
                )
            }
        }

        // -------------------------------------------------------------
        // 11. COMPLETE / DELETE / POSTPONE / PRIORITY (Commands 2, 5, 6, 7, 8)
        // -------------------------------------------------------------
        if (clean.contains("limpar concluidas") || clean.contains("expurgar concluidas") || clean.contains("otimizar sqlite")) {
            return ProcessedResult.ClearCompleted("Expurgando registros concluídos do banco SQLite...")
        }

        if (clean.startsWith("concluir") || clean.startsWith("completar") || clean.startsWith("finalizar") || clean.startsWith("feito")) {
            val target = clean
                .replace(Regex("^(concluir|completar|finalizar|feito)\\s*(tarefa|atividade)?"), "")
                .trim()

            return ProcessedResult.CompleteActivity(
                targetQuery = target,
                responseMessage = if (target.isNotEmpty()) "Identificando e concluindo atividade '$target' no banco SQLite..." else "Qual atividade deseja concluir, senhor?"
            )
        }

        if (clean.startsWith("excluir") || clean.startsWith("apagar") || clean.startsWith("remover") || clean.startsWith("deletar")) {
            val target = clean
                .replace(Regex("^(excluir|apagar|remover|deletar)\\s*(tarefa|atividade)?"), "")
                .trim()

            return ProcessedResult.DeleteActivity(
                targetQuery = target,
                responseMessage = if (target.isNotEmpty()) "Removendo registro '$target' do SQLite..." else "Qual atividade deseja remover?"
            )
        }

        if (clean.startsWith("adiar") || clean.contains("mudar prazo")) {
            val target = clean.replace("adiar", "").replace("tarefa", "").replace("para amanhã", "").replace("para amanha", "").trim()
            val newDue = if (clean.contains("amanhã") || clean.contains("amanha")) "Amanhã" else "Próxima Semana"
            return ProcessedResult.PostponeActivity(
                targetQuery = target,
                newDueDate = newDue,
                responseMessage = "Adiado o prazo de '$target' para $newDue no SQLite."
            )
        }

        if (clean.startsWith("definir prioridade") || clean.startsWith("prioridade") || clean.contains("mudar prioridade")) {
            val newPrio = when {
                clean.contains("alta") || clean.contains("urgente") -> "ALTA"
                clean.contains("baixa") -> "BAIXA"
                else -> "MÉDIA"
            }
            val target = clean
                .replace(Regex("(definir\\s+)?prioridade\\s+(alta|média|media|baixa|urgente)\\s+(em|para|da|de)?"), "")
                .trim()

            return ProcessedResult.UpdatePriority(
                targetQuery = target,
                newPriority = newPrio,
                responseMessage = "Prioridade de '$target' atualizada para $newPrio no SQLite."
            )
        }

        // -------------------------------------------------------------
        // 12. CREATE TASK / ACTIVITY (Command 1)
        // -------------------------------------------------------------
        if (clean.startsWith("criar") || clean.startsWith("lembrar") || clean.startsWith("adicionar") || clean.startsWith("tarefa") || clean.startsWith("agendar") || clean.startsWith("nova atividade")) {
            var rawText = clean
                .replace(Regex("^(criar|lembrar de|lembrar|adicionar|tarefa:|tarefa|agendar|nova atividade:?)"), "")
                .trim()

            if (rawText.isEmpty()) {
                rawText = "Nova Atividade Operacional"
            }

            val priority = when {
                rawText.contains("urgente") || rawText.contains("prioridade alta") || rawText.contains("importante") -> "ALTA"
                rawText.contains("baixa prioridade") || rawText.contains("prioridade baixa") || rawText.contains("quando der") -> "BAIXA"
                else -> "MÉDIA"
            }

            val category = when {
                rawText.contains("estudar") || rawText.contains("livro") || rawText.contains("curso") || rawText.contains("ler") || rawText.contains("aula") -> "Estudo"
                rawText.contains("trabalho") || rawText.contains("relatório") || rawText.contains("reunião") || rawText.contains("email") || rawText.contains("cliente") -> "Trabalho"
                rawText.contains("treino") || rawText.contains("academia") || rawText.contains("remédio") || rawText.contains("água") || rawText.contains("correr") -> "Saúde"
                rawText.contains("código") || rawText.contains("desenvolver") || rawText.contains("projeto") || rawText.contains("app") -> "Projeto"
                rawText.contains("comprar") || rawText.contains("mercado") || rawText.contains("casa") || rawText.contains("pagar") -> "Pessoal"
                else -> "Geral"
            }

            val dueDate = when {
                rawText.contains("amanhã") || rawText.contains("amanha") -> "Amanhã"
                rawText.contains("hoje") -> "Hoje"
                rawText.contains("segunda") -> "Segunda-feira"
                rawText.contains("terça") || rawText.contains("terca") -> "Terça-feira"
                rawText.contains("quarta") -> "Quarta-feira"
                rawText.contains("quinta") -> "Quinta-feira"
                rawText.contains("sexta") -> "Sexta-feira"
                rawText.contains("sábado") || rawText.contains("sabado") -> "Sábado"
                rawText.contains("domingo") -> "Domingo"
                else -> "Hoje"
            }

            val minutesRegex = Regex("(\\d+)\\s*(min|minutos|m|hora|horas|h)")
            val match = minutesRegex.find(rawText)
            val estimatedMinutes = if (match != null) {
                val value = match.groupValues[1].toIntOrNull() ?: 30
                val unit = match.groupValues[2]
                if (unit.startsWith("h")) value * 60 else value
            } else 30

            val cleanTitle = rawText
                .replace(Regex("prioridade (alta|média|media|baixa|urgente)"), "")
                .replace(Regex("(amanhã|amanha|hoje|segunda|terça|terca|quarta|quinta|sexta|sábado|sabado|domingo)"), "")
                .replace(Regex("\\d+\\s*(min|minutos|m|hora|horas|h)"), "")
                .trim()
                .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.ROOT) else it.toString() }
                .ifEmpty { "Atividade Registrada" }

            val activity = ActivityEntity(
                title = cleanTitle,
                description = "Criada via comando de voz/texto no Núcleo Jo'On.",
                category = category,
                priority = priority,
                status = "PENDENTE",
                dueDate = dueDate,
                estimatedMinutes = estimatedMinutes
            )

            return ProcessedResult.CreateActivity(
                activity = activity,
                responseMessage = "Atividade alocada no banco SQLite:\n• Título: $cleanTitle\n• Categoria: $category | Prioridade: $priority\n• Prazo: $dueDate (${estimatedMinutes}m)"
            )
        }

        // -------------------------------------------------------------
        // 13. LIST PENDING / COMPLETED (Commands 3, 4)
        // -------------------------------------------------------------
        if (clean.contains("listar pendentes") || clean.contains("minhas tarefas") || clean.contains("tarefas pendentes") || clean.contains("o que tenho")) {
            val pending = currentActivities.filter { it.status != "CONCLUIDO" }
            return if (pending.isEmpty()) {
                ProcessedResult.ConversationalResponse("Nenhuma tarefa pendente no banco SQLite, senhor. Fila de execução limpa!")
            } else {
                val list = pending.take(6).joinToString("\n") { "• [${it.priority}] ${it.title} (${it.dueDate})" }
                ProcessedResult.ConversationalResponse("Tarefas pendentes no SQLite (${pending.size}):\n$list")
            }
        }

        if (clean.contains("listar concluidas") || clean.contains("tarefas concluidas") || clean.contains("tarefas feitas")) {
            val completed = currentActivities.filter { it.status == "CONCLUIDO" }
            return if (completed.isEmpty()) {
                ProcessedResult.ConversationalResponse("Nenhuma tarefa concluída registrada ainda no histórico.")
            } else {
                val list = completed.take(6).joinToString("\n") { "✓ ${it.title} (${it.category})" }
                ProcessedResult.ConversationalResponse("Histórico de tarefas concluídas (${completed.size}):\n$list")
            }
        }

        // -------------------------------------------------------------
        // 14. TELEMETRY, DATABASE STATS & SYSTEM DIAGNOSTICS (Commands 25, 26, 27, 28, 29)
        // -------------------------------------------------------------
        if (clean.contains("estatisticas de banco") || clean.contains("memoria sqlite") || clean.contains("banco de dados")) {
            val total = currentActivities.size
            val protocolsCount = protocols.size
            return ProcessedResult.DatabaseStats(
                "Métricas do Banco de Dados SQLite:\n• Tabela 'activities': $total registros\n• Tabela 'protocols': $protocolsCount instalados\n• Status da Engine: Nominal / Transações ACID Ativas."
            )
        }

        if (clean.contains("status") || clean.contains("diagnóstico") || clean.contains("diagnostico") || clean.contains("sistema")) {
            val total = currentActivities.size
            val pending = currentActivities.count { it.status != "CONCLUIDO" }
            val completed = currentActivities.count { it.status == "CONCLUIDO" }
            val rate = if (total > 0) (completed * 100) / total else 100

            val statusMsg = buildString {
                appendLine("=== TELEMETRIA DO SISTEMA JO'ON ===")
                appendLine("• Estado: NÚCLEO LOCAL & RECONHECIMENTO DE VOZ ATIVOS")
                appendLine("• Base SQLite: $total tarefas ($pending pendentes, $completed concluídas)")
                appendLine("• Eficiência Operacional: $rate%")
                appendLine("• Protocolos Instalados: ${protocols.size}")
                append("• Python Voice Bridge: Módulo de transcrição pronto.")
            }
            return ProcessedResult.SystemStatus(statusMsg)
        }

        if (clean.contains("briefing") || clean.contains("resumo") || clean.contains("panorama")) {
            val pending = currentActivities.filter { it.status != "CONCLUIDO" }
            val highPriority = pending.filter { it.priority == "ALTA" }

            val msg = buildString {
                appendLine("Briefing operacional do dia:")
                if (pending.isEmpty()) {
                    append("Não há atividades pendentes. O senhor está livre para novos desafios.")
                } else {
                    appendLine("Você possui ${pending.size} atividade(s) pendente(s).")
                    if (highPriority.isNotEmpty()) {
                        appendLine("⚠️ Itens Críticos (Prioridade ALTA):")
                        highPriority.forEach { appendLine("  • ${it.title} (${it.category})") }
                    }
                    append("Recomendo acionar o 'Protocolo Foco' para a primeira tarefa.")
                }
            }
            return ProcessedResult.DailyBriefing(msg)
        }

        // -------------------------------------------------------------
        // 15. MANUAL & HELP (Commands 43, 44)
        // -------------------------------------------------------------
        if (clean.contains("manual") || clean.contains("ajuda") || clean.contains("comandos") || clean.contains("o que pode fazer")) {
            return ProcessedResult.ShowCommandManual(
                responseMessage = "Jo'On possui mais de 40 comandos offline em 10 categorias operacionais. Abra o painel 'Catálogo de Comandos' ou diga: 'criar tarefa...', 'calcular...', 'protocolo foco', 'status', etc."
            )
        }

        if (clean.contains("quem é você") || clean.contains("quem e voce") || clean.contains("sua identidade") || clean.contains("sobre voce")) {
            return ProcessedResult.ConversationalResponse(
                "Eu sou Jo'On, sua Inteligência Operacional Cyber-Jarvis. Opero com processamento de voz contínuo, NLP offline e persistência segura em SQLite local.",
                "IDENTITY"
            )
        }

        // -------------------------------------------------------------
        // 16. GREETINGS & DEFAULTS
        // -------------------------------------------------------------
        if (clean.contains("olá") || clean.contains("ola") || clean.contains("bom dia") || clean.contains("boa tarde") || clean.contains("boa noite") || clean.isEmpty()) {
            val hour = SimpleDateFormat("HH", Locale.getDefault()).format(Date()).toIntOrNull() ?: 12
            val timeGreeting = when (hour) {
                in 5..11 -> "Bom dia"
                in 12..17 -> "Boa tarde"
                else -> "Boa noite"
            }
            return ProcessedResult.ConversationalResponse(
                "$timeGreeting, senhor. Reconhecimento de voz e Núcleo SQLite prontos. Como posso auxiliá-lo?",
                "GREETING"
            )
        }

        return ProcessedResult.ConversationalResponse(
            "Comando recebido no Núcleo Local Jo'On: '$input'. Diga 'manual de comandos' para ver as 40+ diretrizes suportadas."
        )
    }

    private fun handleUnitConversion(clean: String): ProcessedResult.UnitConversion? {
        try {
            // Distance: km to miles / miles to km / km to meters
            if (clean.contains("km") || clean.contains("quilômetros") || clean.contains("quilometros") || clean.contains("milhas")) {
                val numMatch = Regex("(\\d+(?:[.,]\\d+)?)").find(clean) ?: return null
                val value = numMatch.groupValues[1].replace(",", ".").toDouble()
                return if (clean.contains("milhas")) {
                    val res = value * 0.621371
                    ProcessedResult.UnitConversion(value, "km", String.format(Locale.US, "%.2f", res), "milhas", "$value km = ${String.format(Locale.US, "%.2f", res)} milhas")
                } else {
                    val res = value * 1000
                    ProcessedResult.UnitConversion(value, "km", res.toLong().toString(), "metros", "$value km = ${res.toLong()} metros")
                }
            }

            // Weight: kg to lbs / lbs to kg
            if (clean.contains("kg") || clean.contains("quilos") || clean.contains("libras")) {
                val numMatch = Regex("(\\d+(?:[.,]\\d+)?)").find(clean) ?: return null
                val value = numMatch.groupValues[1].replace(",", ".").toDouble()
                val res = value * 2.20462
                return ProcessedResult.UnitConversion(value, "kg", String.format(Locale.US, "%.2f", res), "libras", "$value kg = ${String.format(Locale.US, "%.2f", res)} lbs")
            }

            // Temperature: celsius to fahrenheit
            if (clean.contains("celsius") || clean.contains("fahrenheit")) {
                val numMatch = Regex("(\\d+(?:[.,]\\d+)?)").find(clean) ?: return null
                val value = numMatch.groupValues[1].replace(",", ".").toDouble()
                val fahrenheit = (value * 9 / 5) + 32
                return ProcessedResult.UnitConversion(value, "°C", String.format(Locale.US, "%.1f", fahrenheit), "°F", "$value °C = ${String.format(Locale.US, "%.1f", fahrenheit)} °F")
            }

            // Time: hours to minutes
            if (clean.contains("hora") || clean.contains("horas")) {
                val numMatch = Regex("(\\d+(?:[.,]\\d+)?)").find(clean) ?: return null
                val value = numMatch.groupValues[1].replace(",", ".").toDouble()
                val mins = (value * 60).toLong()
                return ProcessedResult.UnitConversion(value, "horas", mins.toString(), "minutos", "$value horas = $mins minutos")
            }
        } catch (e: Exception) {
            return null
        }
        return null
    }

    private fun evalMath(expr: String): String? {
        return try {
            val sanitized = expr.replace(" ", "").replace(",", ".")
            val tokens = mutableListOf<String>()
            var currentNum = ""
            for (ch in sanitized) {
                if (ch in "+-*/^%") {
                    if (currentNum.isNotEmpty()) {
                        tokens.add(currentNum)
                        currentNum = ""
                    }
                    tokens.add(ch.toString())
                } else if (ch.isDigit() || ch == '.') {
                    currentNum += ch
                }
            }
            if (currentNum.isNotEmpty()) tokens.add(currentNum)
            if (tokens.isEmpty()) return null

            // Pass 1: Power & Modulo & Mult & Div
            val pass1 = mutableListOf<String>()
            var i = 0
            while (i < tokens.size) {
                val token = tokens[i]
                if (token == "*" || token == "/" || token == "^" || token == "%") {
                    val prev = pass1.removeAt(pass1.lastIndex).toDouble()
                    val next = tokens[++i].toDouble()
                    val res = when (token) {
                        "*" -> prev * next
                        "/" -> if (next != 0.0) prev / next else Double.NaN
                        "^" -> Math.pow(prev, next)
                        "%" -> prev % next
                        else -> 0.0
                    }
                    pass1.add(res.toString())
                } else {
                    pass1.add(token)
                }
                i++
            }

            // Pass 2: Addition & Subtraction
            var result = pass1[0].toDouble()
            var j = 1
            while (j < pass1.size) {
                val op = pass1[j]
                val next = pass1[j + 1].toDouble()
                result = if (op == "+") result + next else result - next
                j += 2
            }

            if (result % 1.0 == 0.0) {
                result.toLong().toString()
            } else {
                String.format(Locale.US, "%.2f", result)
            }
        } catch (e: Exception) {
            null
        }
    }
}
