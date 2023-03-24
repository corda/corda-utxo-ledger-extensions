package com.r3.corda.ledger.utxo.testing;

import net.corda.v5.base.types.*;
import net.corda.v5.ledger.common.*;

import java.security.*;

public abstract class ContractTest {

    protected final static MemberX500Name ALICE_NAME = MemberX500Name.parse("CN=Alice, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name BOB_NAME = MemberX500Name.parse("CN=Bob, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name CHARLIE_NAME = MemberX500Name.parse("CN=Charlie, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name DAVE_NAME = MemberX500Name.parse("CN=Dave, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name EVE_NAME = MemberX500Name.parse("CN=Eve, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name BANK_A_NAME = MemberX500Name.parse("CN=Bank A, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name BANK_B_NAME = MemberX500Name.parse("CN=Bank B, OU=Test Dept, O=R3, L=London, C=GB");
    protected final static MemberX500Name NOTARY_NAME = MemberX500Name.parse("CN=Notary, OU=Test Dept, O=R3, L=London, C=GB");

    protected final static PublicKey ALICE_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey BOB_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey CHARLIE_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey DAVE_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey EVE_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey BANK_A_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey BANK_B_KEY = ContractTestUtils.createRandomPublicKey();
    protected final static PublicKey NOTARY_KEY = ContractTestUtils.createRandomPublicKey();
    
    protected final static Party ALICE_PARTY = new Party(ALICE_NAME, ALICE_KEY);
    protected final static Party BOB_PARTY = new Party(BOB_NAME, BOB_KEY);
    protected final static Party CHARLIE_PARTY = new Party(CHARLIE_NAME, CHARLIE_KEY);
    protected final static Party DAVE_PARTY = new Party(DAVE_NAME, DAVE_KEY);
    protected final static Party EVE_PARTY = new Party(EVE_NAME, EVE_KEY);
    protected final static Party BANK_A_PARTY = new Party(BANK_A_NAME, BANK_A_KEY);
    protected final static Party BANK_B_PARTY = new Party(BANK_B_NAME, BANK_B_KEY);
    protected final static Party NOTARY_PARTY = new Party(NOTARY_NAME, NOTARY_KEY);
}
