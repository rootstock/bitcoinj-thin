package co.rsk.bitcoinj.script;

import co.rsk.bitcoinj.core.VerificationException;
import java.util.ArrayList;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.spongycastle.util.encoders.Hex;

public class ErpFederationRedeemScriptParser extends StandardRedeemScriptParser {
    private static final Logger logger = LoggerFactory.getLogger(FastBridgeRedeemScriptParser.class);

    public ErpFederationRedeemScriptParser(
        ScriptType scriptType,
        List<ScriptChunk> redeemScript,
        List<ScriptChunk> chunks
    ) {
        super(
            scriptType,
            extractStandardRedeemScriptChunksFromErpRedeemScript(redeemScript),
            chunks
        );
        this.multiSigType = MultiSigType.ERP_FED;
    }

    public static List<ScriptChunk> extractStandardRedeemScriptChunksFromErpRedeemScript(
        List<ScriptChunk> chunks
    ) {
        List<ScriptChunk> chunksForRedeem = new ArrayList<>();

        int i = 1;
        while (!chunks.get(i).equalsOpCode(ScriptOpCodes.OP_ELSE)) {
            chunksForRedeem.add(chunks.get(i));
            i ++;
        }

        chunksForRedeem.add(new ScriptChunk(ScriptOpCodes.OP_CHECKMULTISIG, null));

        return chunksForRedeem;
    }

    public static Script createErpRedeemScript(
        Script defaultFederationRedeemScript,
        Script erpFederationRedeemScript,
        String csvValue
    ) {
        ScriptBuilder scriptBuilder = new ScriptBuilder();

        return scriptBuilder.op(ScriptOpCodes.OP_NOTIF)
            .addChunks(removeOpCheckMultisig(defaultFederationRedeemScript))
            .op(ScriptOpCodes.OP_ELSE)
            .data(Hex.decode(csvValue))
            .op(ScriptOpCodes.OP_CHECKLOCKTIMEVERIFY)
            .op(ScriptOpCodes.OP_DROP)
            .addChunks(removeOpCheckMultisig(erpFederationRedeemScript))
            .op(ScriptOpCodes.OP_ENDIF)
            .op(ScriptOpCodes.OP_CHECKMULTISIG)
            .build();
    }

    private static List<ScriptChunk> removeOpCheckMultisig(Script redeemScript) {
        List<ScriptChunk> chunksWithoutCheckMultisig = new ArrayList<>();
        chunksWithoutCheckMultisig.addAll(redeemScript.getChunks());

        // Remove the last element that represents CHECKMULTISIG op code
        chunksWithoutCheckMultisig.remove(chunksWithoutCheckMultisig.size() - 1);

        return chunksWithoutCheckMultisig;
    }
}