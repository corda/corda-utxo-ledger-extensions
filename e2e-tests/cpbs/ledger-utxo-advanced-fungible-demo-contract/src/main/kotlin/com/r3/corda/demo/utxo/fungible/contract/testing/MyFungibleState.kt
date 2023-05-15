package com.r3.corda.demo.utxo.fungible.contract.testing

import com.r3.corda.ledger.utxo.fungible.FungibleState
import com.r3.corda.ledger.utxo.fungible.NumericInteger
import com.r3.corda.ledger.utxo.ownable.OwnableState
import net.corda.v5.ledger.utxo.BelongsToContract
import java.security.PublicKey

@BelongsToContract(MyFungibleContract::class)
interface MyFungibleState : FungibleState<NumericInteger>, OwnableState
