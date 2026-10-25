package app.xposed.patches.twitter.badges

import app.morphe.patcher.Fingerprint
import app.xposed.patches.twitter.Constants.HOOKS_CLASS

/** TweetHeaderView.setData(name, username, timestamp, badges). */
internal object TweetHeaderSetDataFingerprint : Fingerprint(
    definingClass = "Lcom/twitter/ui/tweet/TweetHeaderView;",
    returnType = "V",
    parameters = listOf("Ljava/lang/String;", "Ljava/lang/String;", "Ljava/lang/String;", "Ljava/util/List;"),
)

/** UsernameBadge.toString(); its class holds the (ImageSpan, String) constructor. */
internal object UsernameBadgeToStringFingerprint : Fingerprint(
    name = "toString",
    strings = listOf("UsernameBadge(span="),
)

/** DefaultAboutProfileRepository's user mapper; other mappers share the string. */
internal object AboutProfileRepositoryFingerprint : Fingerprint(
    strings = listOf("Missing GraphqlUser.screen_name for "),
    custom = { _, classDef ->
        classDef.methods.any { method ->
            method.parameterTypes.map { it.toString() } ==
                listOf("Ljava/lang/String;", "Lkotlin/coroutines/jvm/internal/ContinuationImpl;")
        }
    },
)

/** GraphqlApiImpl constructor; the instance is the app's authenticated GraphQL client. */
internal object GraphqlApiConstructorFingerprint : Fingerprint(
    name = "<init>",
    strings = listOf("apolloService", "transactionEnforcer", "accountSuspensionHandler"),
)

internal object UsernameBadgeClassExtensionFingerprint : Fingerprint(
    definingClass = HOOKS_CLASS,
    name = "usernameBadgeClass",
)

internal object AboutRepositoryClassExtensionFingerprint : Fingerprint(
    definingClass = HOOKS_CLASS,
    name = "aboutRepositoryClass",
)
