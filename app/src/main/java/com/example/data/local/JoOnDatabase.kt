package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        ActivityEntity::class,
        ProtocolEntity::class,
        LogEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class JoOnDatabase : RoomDatabase() {
    abstract fun activityDao(): ActivityDao
    abstract fun protocolDao(): ProtocolDao
    abstract fun logDao(): LogDao

    companion object {
        @Volatile
        private var INSTANCE: JoOnDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): JoOnDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    JoOnDatabase::class.java,
                    "joon_assistant.db"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialData(database)
                    }
                }
            }

            private suspend fun populateInitialData(database: JoOnDatabase) {
                val activityDao = database.activityDao()
                val protocolDao = database.protocolDao()
                val logDao = database.logDao()

                // Default activities
                val defaultActivities = listOf(
                    ActivityEntity(
                        title = "Revisar diretrizes operacionais Jo'On",
                        description = "Explorar os módulos de inteligência Offline e Online do assistente.",
                        category = "Sistema",
                        priority = "ALTA",
                        status = "PENDENTE",
                        dueDate = "Hoje",
                        estimatedMinutes = 15
                    ),
                    ActivityEntity(
                        title = "Executar Protocolo de Foco Alpha",
                        description = "Iniciar sessão de 25 minutos de produtividade profunda.",
                        category = "Foco",
                        priority = "MÉDIA",
                        status = "PENDENTE",
                        dueDate = "Hoje",
                        estimatedMinutes = 25
                    ),
                    ActivityEntity(
                        title = "Sincronizar base SQLite local",
                        description = "Armazenamento estruturado de atividades e registros de comando.",
                        category = "Dados",
                        priority = "BAIXA",
                        status = "CONCLUIDO",
                        dueDate = "Hoje",
                        estimatedMinutes = 5,
                        completedAt = System.currentTimeMillis()
                    )
                )
                activityDao.insertActivities(defaultActivities)

                // Default Protocols
                val defaultProtocols = listOf(
                    ProtocolEntity(
                        id = "PROTO_FOCUS_ALPHA",
                        name = "Protocolo Foco Alpha",
                        description = "Isolamento cognitivo e sprint de 25 minutos com alta densidade de execução.",
                        iconName = "bolt",
                        category = "Produtividade",
                        stepsList = "Definir objetivo único do sprint|Silenciar notificações externas|Executar bloco contínuo de 25m|Registrar progresso na base SQLite",
                        estimatedDuration = "25 min"
                    ),
                    ProtocolEntity(
                        id = "PROTO_DAILY_BRIEFING",
                        name = "Protocolo Briefing Matinal",
                        description = "Alinhamento tático diário: varredura de pendências, metas críticas e telemetria.",
                        iconName = "wb_sunny",
                        category = "Organização",
                        stepsList = "Revisar atividades de prioridade ALTA|Calcular tempo total alocado|Definir as 3 metas essenciais|Testar estado dos módulos Jo'On",
                        estimatedDuration = "10 min"
                    ),
                    ProtocolEntity(
                        id = "PROTO_DEEP_WORK",
                        name = "Protocolo Trabalho Profundo",
                        description = "Sessão intensiva para projetos complexos com divisão automática em sub-etapas.",
                        iconName = "psychology",
                        category = "Engenharia",
                        stepsList = "Mapear dependências do projeto|Criar tarefas modulares no SQLite|Executar etapa 1 sem interrupções|Validar entrega parcial",
                        estimatedDuration = "50 min"
                    ),
                    ProtocolEntity(
                        id = "PROTO_EVENING_DEBRIEF",
                        name = "Protocolo Encerramento Noturno",
                        description = "Consolidação de atividades concluídas, limpeza do backlog e preparação para amanhã.",
                        iconName = "nights_stay",
                        category = "Revisão",
                        stepsList = "Marcar tarefas finalizadas|Mover itens pendentes para o dia seguinte|Registrar insights no diário de bordo|Otimizar banco SQLite",
                        estimatedDuration = "15 min"
                    )
                )
                protocolDao.insertProtocols(defaultProtocols)

                // Initial greeting log
                logDao.insertLog(
                    LogEntity(
                        sender = "JOON",
                        message = "Sistemas Jo'On inicializados. Núcleo Offline e Ponte Neural Online operacionais. Ao seu dispor, senhor.",
                        mode = "OFFLINE",
                        actionTag = "SYSTEM_INIT"
                    )
                )
            }
        }
    }
}
