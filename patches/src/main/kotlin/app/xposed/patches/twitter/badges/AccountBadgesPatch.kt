package app.xposed.patches.twitter.badges

import app.morphe.patcher.extensions.InstructionExtensions.addInstructions
import app.morphe.patcher.extensions.InstructionExtensions.instructions
import app.morphe.patcher.patch.bytecodePatch
import app.xposed.patches.twitter.Constants.COMPATIBILITY_X
import app.xposed.patches.twitter.Constants.EXTENSION_PATH
import app.xposed.patches.twitter.Constants.HOOKS_CLASS
import com.android.tools.smali.dexlib2.Opcode

private fun String.toJavaClassName() = substring(1, length - 1).replace('/', '.')

private fun returnString(value: String) =
    """
    const-string v0, "$value"
    return-object v0
    """

@Suppress("unused")
val accountBadgesPatch = bytecodePatch(
    name = "Account location badges",
    description = "Shows X-Posed style country badges next to names. " +
        "Data comes from the X-Posed community cache, falling back to X's \"About this account\".",
    default = true,
) {
    compatibleWith(COMPATIBILITY_X)

    extendWith(EXTENSION_PATH)

    execute {
        // Badges are ImageSpans appended after the display name, so adding ours
        // to the list lets X measure, ellipsize and draw it like a verified badge.
        TweetHeaderSetDataFingerprint.method.addInstructions(
            0,
            """
            invoke-static/range { p0 .. p4 }, $HOOKS_CLASS->onSetHeaderData(Landroid/view/View;Ljava/lang/String;Ljava/lang/String;Ljava/lang/String;Ljava/util/List;)Ljava/util/List;
            move-result-object p4
            """,
        )

        // Obfuscated class names are resolved here and handed to the extension.
        UsernameBadgeClassExtensionFingerprint.method.addInstructions(
            0,
            returnString(UsernameBadgeToStringFingerprint.originalClassDef.type.toJavaClassName()),
        )
        AboutRepositoryClassExtensionFingerprint.method.addInstructions(
            0,
            returnString(AboutProfileRepositoryFingerprint.originalClassDef.type.toJavaClassName()),
        )

        GraphqlApiConstructorFingerprint.method.apply {
            addInstructions(
                instructions.indexOfLast { it.opcode == Opcode.RETURN_VOID },
                "invoke-static { p0 }, $HOOKS_CLASS->onGraphqlApi(Ljava/lang/Object;)V",
            )
        }
    }
}
