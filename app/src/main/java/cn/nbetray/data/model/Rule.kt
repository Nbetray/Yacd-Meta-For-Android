package cn.nbetray.data.model

data class RulesResponse(
    val rules: List<Rule>
)

data class Rule(
    val type: String,
    val payload: String,
    val proxy: String,
    val size: Int = 0
)

data class RuleProvidersResponse(
    val providers: Map<String, RuleProvider>
)

data class RuleProvider(
    val name: String,
    val type: String,
    val behavior: String,
    val ruleCount: Int,
    val updatedAt: String?,
    val vehicleType: String
)
