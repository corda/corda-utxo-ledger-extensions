# Corda 5 Advanced UTXO Ledger Extensions

The Corda 5 Advanced UTXO Ledger Extensions library provides several powerful features to Corda 5's UTXO ledger. These features have been selected and designed to solve common problems that CorDapp developers face when building states and contracts on Corda.

## Feature Overview

The following definitions provide an overview of each major feature or component that has been implemented in the Corda 5 Advanced UTXO Ledger Extensions library. These features can be used together; for example, a state could be designed to be fungible, issuable and ownable.

### Chainable

Chainable states represent strictly linear state chains, where every state in the chain points to the previous state in the chain. This could be thought of as a similar concept to a blockchain, where each new block points to the previous block.

### Fungible

Fungible states represent states that have a scalar numeric quantity, and can be split, merged and mutually exchanged with other fungible states of the same class. Fungible states represent the building blocks for states like tokens.

### Identifiable

Identifiable states represent states that have a unique identifier that is guaranteed unique at the network level. Identifiable states are designed to evolve over time, where unique identifiers can be used to resolve the history of the identifiable state.

### Issuable

Issuable states represent states that have an issuer. Typically an issuer is responsible for signing transactions where issuable states are issued or redeemed.

### Ownable

Ownable states represent states that have an owner. Typically an owner is responsible for signing transactions where ownable states are transferred from one owner to another.

## Design Motivations

Over the years as Corda has grown and matured, we've identified several common patterns and practices for Corda state and contract design; for example tokens (Corda 4 addressed this with the Token SDK).

What we noticed was that many of these patterns and practices either end up being implemented repeatedly in different business application scenarios, and abstractions of these patterns and practices ended up being too rigid and didn't fit certain business application scenarios.

The Corda 5 Advanced UTXO Ledger Extensions library aims to provide abstractions at several levels, allowing CorDapp developers to opt-in to API features as required.

## Contract Design

In order to fully understand the design motivations, first we must understand some of the challenges commonly faced by CorDapp developers.

### Basic Contract Design

We'll start by taking a look at a trivial contract implementation, below. The following contract defines three commands; `Create`, `Update` and `Delete`. The `verify` function delegates these command types to `verifyCreate`, `verifyUpdate` and `verifyDelete` functions respectively; for example:

```java
public final class ExampleContract implements Contract {

    private interface ExampleContractCommand extends Command { }
  
    public static class Create implements ExampleContractCommand { }
    public static class Update implements ExampleContractCommand { }
    public static class Delete implements ExampleContractCommand { }

    @Override
    public void verify(UtxoLedgerTransaction transaction) {
        
        List<? extends ExampleContractCommand> commands = transaction
                .getCommands(ExampleContractCommand.class);
        
        for (ExampleContractCommand command : commands) {
            if (command instanceof Create) verifyCreate(transaction);
            else if (command instanceof Update) verifyUpdate(transaction);
            else if (command instanceof Delete) verifyDelete(transaction);
            else throw new IllegalStateException("Unrecognised command type.");
        }
    }

    private void verifyCreate(UtxoLedgerTransaction transaction) {
        // Verify Create constraints
    }

    private void verifyUpdate(UtxoLedgerTransaction transaction) {
        // Verify Update constraints
    }

    private void verifyDelete(UtxoLedgerTransaction transaction) {
        // Verify Delete constraints
    }
}
```

Designing a contract like this will suffice in many cases. Assuming that the constraints have been implemented correctly then the contract functionality and design is perfectly acceptable.

However, there are cases where this design approach no longer fits the design goals of the system being implemented; specifically, in regard to contract extensibility, it's currently not possible to extend this contract to support additional constraints.

### Derivable Contract Design

The following contract refactors the above to support the ability to derive contracts, and provide additional constraints in a secure and controlled way.

The contract still provides the same three commands; `Create`, `Update` and `Delete`. The `verify` function delegates these command types to `verifyCreate`, `verifyUpdate` and `verifyDelete` functions respectively, which in turn call `onVerifyCreate`, `onVerifyUpdate` and `onVerifyDelete` respectively.

Note that the `verify` function has been marked `final`. This change is necessary as it prevents derived contract implementations from circumventing the base contract rules; for example;

```java
public class ExampleContract implements Contract {

    private interface ExampleContractCommand extends Command { }

    public static class Create implements ExampleContractCommand { }
    public static class Update implements ExampleContractCommand { }
    public static class Delete implements ExampleContractCommand { }

    @Override
    public final void verify(UtxoLedgerTransaction transaction) {

        List<? extends ExampleContractCommand> commands = transaction
                .getCommands(ExampleContractCommand.class);

        for (ExampleContractCommand command : commands) {
            if (command instanceof Create) verifyCreate(transaction);
            else if (command instanceof Update) verifyUpdate(transaction);
            else if (command instanceof Delete) verifyDelete(transaction);
            else throw new IllegalStateException("Unrecognised command type.");
        }
    }
    
    protected void onVerifyCreate(UtxoLedgerTransaction transaction) { }
    protected void onVerifyUpdate(UtxoLedgerTransaction transaction) { }
    protected void onVerifyDelete(UtxoLedgerTransaction transaction) { }    

    private void verifyCreate(UtxoLedgerTransaction transaction) {
      	// Verify base Create constraints
        // Then verify additional Create constraints implemented by derived contracts
        onVerifyCreate(transaction);
    }

    private void verifyUpdate(UtxoLedgerTransaction transaction) {
        // Verify base Update constraints
        // Then verify additional Update constraints implemented by derived contracts
        onVerifyUpdate(transaction);
    }

    private void verifyDelete(UtxoLedgerTransaction transaction) {
        // Verify base Delete constraints
        // Then verify additional Delete constraints implemented by derived contracts
        onVerifyDelete(transaction);
    }
}
```

As demonstrated, refactoring a contract like this allows CorDapp implementors to derive from the contract, allowing additional constraints which will be verified in additional to the constraints specified by the base contract.

However, there are still some outstanding issues with this design, where this design approach no longer fits the design goals of the system being implemented.

The problem really lies in the verify function; for example:

```java
public final void verify(UtxoLedgerTransaction transaction) {

    List<? extends ExampleContractCommand> commands = transaction
            .getCommands(ExampleContractCommand.class);

    for (ExampleContractCommand command : commands) {
        if (command instanceof Create) verifyCreate(transaction);
        else if (command instanceof Update) verifyUpdate(transaction);
        else if (command instanceof Delete) verifyDelete(transaction);
        else throw new IllegalStateException("Unrecognised command type.");
    }
}
```

The `verify` function is marked `final` for security reasons, and therefore additional commands cannot be added to the contract; for example, the contract may wish to describe multiple ways to _Update_ a state, or set of states. Since the contract only defines a single `Update` command, there can only be one mechanism to perform updates.

The second problem lies in the commands themselves, and their names. `Create`, `Update` and `Delete` are very ambiguous names, which almost certainly won't make sense depending on the context of the contract being implemented.

### Delegated Command Design

In the contracts above, the commands are nothing more than marker classes; effectively they are cases in a switch statement, which allow the contract's `verify` function to delegate responsibility of specific contract constraints to other functions, such as `verifyCreate`, `verifyUpdate` and `verifyDelete`.

Instead, we could implement the `verify` function on the command itself. Instead of being an empty marker class, this gives the command responsibility, as it becomes responsible for implementing its associated contract verification constraints.

In this case, we define a `VerifiableCommand` interface with a `verify` function; for example:

```java
public interface VerifiableCommand extends Command {
    void verify(UtxoLedgerTransaction transaction);
}
```

Now that we have a command which itself can implement contract verification constraints, we can use this as the basis for the `ExampleContractCommand` class. Notice that this now needs to be a class rather than an interface, because we need to be in complete control of its implementations for security.

We achieve this by making the default constructor package-private, so that only commands within the same package can extend it; for example:

```java
public class ExampleContractCommand implements VerifiableCommand {
  ExampleContractCommand() { }
}
```

Next, we can implement this interface as `Create`, `Update` and `Delete` commands; for example:

```java
public class Create extends ExampleContractCommand {

    @Override
    public final void verify(UtxoLedgerTransaction transaction) {
        // Verify base Create constraints
        // Then verify additional Create constraints implemented in derived commands
        onVerify(transaction);
    }

    protected void onVerify(UtxoLedgerTransaction transaction) { }
}

public class Update extends ExampleContractCommand {

    @Override
    public final void verify(UtxoLedgerTransaction transaction) {
        // Verify base Update constraints
        // Then verify additional Update constraints implemented in derived commands
        onVerify(transaction);
    }

    protected void onVerify(UtxoLedgerTransaction transaction) { }
}

public class Delete extends ExampleContractCommand {

    @Override
    public final void verify(UtxoLedgerTransaction transaction) {
        // Verify base Delete constraints
        // Then verify additional Delete constraints implemented in derived commands
        onVerify(transaction);
    }

    protected void onVerify(UtxoLedgerTransaction transaction) { }
}
```

Note that the `Create`, `Update` and `Delete` commands are not marked `final`, therefore we can extend the contract verification constraints from these points, but we can't extend from `ExampleContractCommand`.

### Delegated Contract Design

As we have now delegated contract verification constraint logic to the commands themselves, we must also refctor the contract to support this delegation. The contract implementation in this case becomes incredibly simple, since it's no longer responsible for defining contract verification constraints; for example:

```java
public final class ExampleContract implements Contract {

    @Override
    public void verify(UtxoLedgerTransaction transaction) {

        List<? extends ExampleContractCommand> commands = transaction
                .getCommands(ExampleContractCommand.class);

        for (ExampleContractCommand command : commands) {
            command.verify(transaction);
        }
    }
}
```

This design addresses the outstanding issues in regard to being able to extend a contract with multiple commands, and being able to give command's names that make sense in the context that they're used; for example:

```java
class Mint extends Create { ... }
class Issue extends Update { ... }
class Transfer extends Update { ... }
class Exchange extends Update { ... }
class Redeem extends Update { ... }
class Burn extends Delete { ... }
```

Note that the contract now supports five different command types, each of which implements different constraints and derives from `Create`, `Update`, or `Delete`.

## Advanced Contract Design

Ultimately, all the contract design issues that have been highlighted above are implemented by the Corda 5 Advanced UTXO Extensions library, and into all the specific implementations; for example chainable, fungible and identifiable contracts.

## Base API

**Module:** base

**Package:** com.r3.corda.ledger.utxo.base

The base API provides the underlying component model for designing extensible contracts with delegated contract verification constraint logic, as well as some other components which allow CorDapp developers to be more expressive and better express intent throughout their applications.

## Chainable API

**Module:** chainable

**Package:** com.r3.corda.ledger.utxo.chainable

The chainable API provides the component model for designing chainable states and contracts. Chainable states represent strictly linear state chains, where every state in the chain points to the previous state in the chain. This could be thought of as a similar concept to a blockchain, where each new block points to the previous block.

### Designing a Chainable State

A chainable state can be implemented by implementing the `ChainableState<T>` interface; for example:

```java
@BelongsToContract(ExampleChainableContract.class)
public final class ExampleChainableState extends ChainableState<ExampleChainableState> {
  
  @Nullable
  private final StaticPointer<ExampleChainableState> pointer;
  
  public ExampleChainableState(@NotNull final StaticPointer<ExampleChainableState> pointer) {
    this.pointer = pointer;
  }
  
  @Nullable
  public StaticPointer<ExampleChainableState> getPreviousStatePointer() {
    return pointer;
  }
  
  @NotNull
  public List<PublicKey> getParticipants() {
    return List.of(...);
  }
}
```

### Designing Chainable Commands

Chainable commands support creating, updating and deleting chainable states.

The `ChainableContractCreateCommand` supports creating new chainable states and will verify the following constraints:

-   On chainable state(s) creating, at least one chainable state must be created.
-   On chainable state(s) creating, the previous state pointer of every created chainable state must be null.

```java
public final class Create extends ChainableContractCreateCommand<ExampleChainableState> {
  
  @NotNull
  public Class<ExampleChainableState> getContractStateType() {
    return ExampleChainableState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Create constraints
  }
}
```

The `ChainableContractUpdateCommand` supports updating existing chainable states and will verify the following constraints:

-   On chainable state(s) updating, at least one chainable state must be consumed.
-   On chainable state(s) updating, at least one chainable state must be created.
-   On chainable state(s) updating, the previous state pointer of every created chainable state must not be null.
-   On chainable state(s) updating, the previous state pointer of every created chainable state must be pointing to exactly one consumed chainable state, exclusively.

```java
public final class Update extends ChainableContractUpdateCommand<ExampleChainableState> {
  
  @NotNull
  public Class<ExampleChainableState> getContractStateType() {
    return ExampleChainableState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Update constraints
  }
}
```

The `ChainableContractDeleteCommand` supports deleting existing chainable states and will verify the following constraints:

-   On chainable state(s) deleting, at least one chainable state must be consumed.

```java
public final class Delete extends ChainableContractDeleteCommand<ExampleChainableState> {
  
  @NotNull
  public Class<ExampleChainableState> getContractStateType() {
    return ExampleChainableState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Delete constraints
  }
}
```

### Designing a Chainable Contract

A chainable contract can be implemented by extending the `ChainableContract` class; for example:

```java
public final class ExampleChainableContract extends ChainableContract {
  
  @Override
  public List<Class<? extends ChainableContractCommand<?>>> getPermittedCommandTypes() {
    return List.of(Create.class, Update.class, Delete.class);
  }
}
```

## Fungible API

**Module:** fungible

**Package:** com.r3.corda.ledger.utxo.fungible

The fungible API provides the component model for designing fungible states and contracts. Fungible states represent states that have a scalar numeric quantity, and can be split, merged and mutually exchanged with other fungible states of the same class. Fungible states represent the building blocks for states like tokens.

### Designing a Fungible State

A fungible state can be implemented by implementing the `FungibleState<T>` interface; for example:

```java
public final class ExampleFungibleState extends FungibleState<NumericDecimal> {
  
  @NotNull
  private final NumericDecimal quantity;
  
  public ExampleFungibleState(@NotNull final NumericDecimal quantity) {
    this.quantity = quantity;
  }
  
  @NotNull
  public NumericDecimal getQuantity() {
    return quantity;
  }
  
  @NotNull
  public List<PublicKey> getParticipants() {
    return List.of(...);
  }
  
  @Override
  public boolean isFungibleWith(@NotNull final FungibleState<NumericDecimal> other) {
    return this == other || other instanceof ExampleFungibleState // && other fungibility rules.
  }
}
```

### Designing Fungible Commands

Fungible commands support creating, updating and deleting fungible states.

The `FungibleContractCreateCommand` supports creating new fungible states and will verify the following constraints:

-   On fungible state(s) creating, at least one fungible state must be created.
-   On fungible state(s) creating, the quantity of every created fungible state must be greater than zero.

```java
public final class Create extends FungibleContractCreateCommand<ExampleFungibleState> {
  
  @NotNull
  public Class<ExampleFungibleState> getContractStateType() {
    return ExampleFungibleState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Create constraints
  }
}
```

The `FungibleContractUpdateCommand` supports updating existing fungible states and will verify the following constraints:

-   On fungible state(s) updating, at least one fungible state must be consumed.
-   On fungible state(s) updating, at least one fungible state must be created.
-   On fungible state(s) updating, the quantity of every created fungible state must be greater than zero.
-   On fungible state(s) updating, the sum of the unscaled values of the consumed states must be equal to the sum of the unscaled values of the created states.
-   On fungible state(s) updating, the sum of the consumed states that are fungible with each other must be equal to the sum of the created states that are fungible with each other.

```java
public final class Update extends FungibleContractUpdateCommand<ExampleFungibleState> {
  
  @NotNull
  public Class<ExampleFungibleState> getContractStateType() {
    return ExampleFungibleState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Update constraints
  }
}
```

The `FungibleContractDeleteCommand` supports deleting existing fungible states and will verify the following constraints:

-   On fungible state(s) deleting, at least one fungible state input must be consumed.
-   On fungible state(s) deleting, the sum of the unscaled values of the consumed states must be greater than the sum of the unscaled values of the created states.
-   On fungible state(s) deleting, the sum of consumed states that are fungible with each other must be greater than the sum of the created states that are fungible with each other.

```java
public final class Delete extends FungibleContractDeleteCommand<ExampleFungibleState> {
  
  @NotNull
  public Class<ExampleFungibleState> getContractStateType() {
    return ExampleFungibleState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Delete constraints
  }
}
```

### Designing a Fungible Contract

A fungible contract can be implemented by extending the `FungibleContract` class; for example:

```java
public final class ExampleFungibleContract extends FungibleContract {
  
  @Override
  public List<Class<? extends FungibleContractCommand<?>>> getPermittedCommandTypes() {
    return List.of(Create.class, Update.class, Delete.class);
  }
}
```

## Identifiable API

**Module:** identifiable

**Package:** com.r3.corda.ledger.utxo.identifiable

The identifiable API provides the component model for designing identifiable states and contracts. Identifiable states represent states that have a unique identifier that is guaranteed unique at the network level. Identifiable states are designed to evolve over time, where unique identifiers can be used to resolve the history of the identifiable state.

### Designing an Identifiable State

An identifiable state can be implemented by implementing the `IdentifiableState` interface; for example:

```java
public final class ExampleIdentifiableState extends IdentifiableState {
  
  @Nullable
  private final StateRef id;
  
  public ExampleIdentifiableState(@Nullable final StateRef id) {
    this.id = id;
  }
  
  @Nullable
  public StateRef getId() {
    return id;
  }
  
  @NotNull
  public List<PublicKey> getParticipants() {
    return List.of(...);
  }
}
```

### Designing Identifiable Commands

Identifiable commands support creating, updating and deleting identifiable states.

The `IdentifiableContractCreateCommand` supports creating new identifiable states and will verify the following constraints:

-   On identifiable state(s) creating, at least one identifiable state must be created.

```java
public final class Create extends IdentifiableContractCreateCommand<ExampleIdentifiableState> {
  
  @NotNull
  public Class<ExampleIdentifiableState> getContractStateType() {
    return ExampleIdentifiableState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Create constraints
  }
}
```

The `IdentifiableContractUpdateCommand` supports updating existing identifiable states and will verify the following constraints:

-   On identifiable state(s) updating, at least one identifiable state must be consumed.
-   On identifiable state(s) updating, at least one identifiable state must be created.
-   On identifiable state(s) updating, each created identifiable state's identifier must match one consumed identifiable state's state ref or identifier, exclusively.

```java
public final class Update extends IdentifiableContractUpdateCommand<ExampleIdentifiableState> {
  
  @NotNull
  public Class<ExampleIdentifiableState> getContractStateType() {
    return ExampleIdentifiableState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Update constraints
  }
}
```

The `IdentifiableContractDeleteCommand` supports deleting existing identifiable states and will verify the following constraints:

-   On identifiable state(s) deleting, at least one identifiable state must be consumed.

```java
public final class Delete extends IdentifiableContractDeleteCommand<ExampleIdentifiableState> {
  
  @NotNull
  public Class<ExampleIdentifiableState> getContractStateType() {
    return ExampleIdentifiableState.class;
  }
  
  @Override
  protected void onVerify(@NotNull final UtxoLedgerTransaction transaction) {
    // Verify additional Delete constraints
  }
}
```

### Designing an Identifiable Contract

An identifiable contract can be implemented by extending the `IdentifiableContract` class; for example:

```java
public final class ExampleIdentifiableContract extends IdentifiableContract {
  
  @Override
  public List<Class<? extends IdentifiableContractCommand<?>>> getPermittedCommandTypes() {
    return List.of(Create.class, Update.class, Delete.class);
  }
}
```
