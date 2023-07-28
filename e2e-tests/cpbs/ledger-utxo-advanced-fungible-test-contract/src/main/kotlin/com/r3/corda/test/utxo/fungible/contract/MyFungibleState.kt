package com.r3.corda.test.utxo.fungible.contract

import com.r3.corda.ledger.utxo.fungible.FungibleState
import com.r3.corda.ledger.utxo.fungible.NumericInteger
import com.r3.corda.ledger.utxo.ownable.OwnableState
import net.corda.v5.ledger.utxo.BelongsToContract

@BelongsToContract(MyFungibleContract::class)
interface MyFungibleState : FungibleState<NumericInteger>, OwnableState
