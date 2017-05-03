package fdtmc;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class FDTMC {

    public static final String INITIAL_LABEL = "initial";
    public static final String SUCCESS_LABEL = "success";
    public static final String ERROR_LABEL = "error";

	private Set<State> states;
    private State initialState;
    private State successState;
    private State errorState;
	private String variableName;
	private int index;
	private Map<State, List<Transition>> transitionSystem;
	private Map<String, List<Interface>> interfaces;
	private FDTMCInline fdtmcInline; 


	public FDTMC() {
		states = new LinkedHashSet<State>();
		initialState = null;
		variableName = null;
		index = 0;
		transitionSystem = new LinkedHashMap<State, List<Transition>>();
		interfaces = new LinkedHashMap<String, List<Interface>>();
		fdtmcInline = new FDTMCInline();
	}

	public Collection<State> getStates() {
		return states;
	}

	public void setVariableName(String name) {
		variableName = name;
	}

	public String getVariableName() {
		return variableName;
	}

	public int getVariableIndex() {
		return index;
	}
	
	public Map<State, List<Transition>> getTransitionSystem() {
		return transitionSystem;
	}

	public void setTransitionSystem(Map<State, List<Transition>> transitionSystem) {
		this.transitionSystem = transitionSystem;
	}
	
	public Map<String, List<Interface>> getInterfaces() {
		return interfaces;
	}

	public void setInterfaces(Map<String, List<Interface>> interfaces) {
		this.interfaces = interfaces;
	}
	
	public FDTMCInline getFdtmcInline() {
		return fdtmcInline;
	}

	public void setFdtmcInline(FDTMCInline fdtmcInline) {
		this.fdtmcInline = fdtmcInline;
	}

	public State createState() {
		State state = new State();
		state.setVariableName(variableName);
		state.setIndex(index);
		addState(state);
		return state;
	}
	
	private void addState(State state){
		getStates().add(state);
		getTransitionSystem().put(state, null);
		if (index == 0)
			setInitialState(state);
		incrementIndex();	
	}
	
	private int incrementIndex(){
		return index++;
	}

	public State createState(String label) {
		State state = createState();
		state.setLabel(label);
		return state;
	}

    public State createInitialState() {
        State initial = createState();
        setInitialState(initial);
        return initial;
    }

    private void setInitialState(State initialState) {
        if (this.getInitialState() != null) {
            this.getInitialState().setLabel(null);
        }
        this.initialState = initialState;
        initialState.setLabel(INITIAL_LABEL);
    }

    public State getInitialState() {
        return this.initialState;
    }

    public State createSuccessState() {
        State success = createState();
        setSuccessState(success);
        return success;
    }

    private void setSuccessState(State successState) {
        this.successState = successState;
        successState.setLabel(SUCCESS_LABEL);
    }

    public State getSuccessState() {
        return successState;
    }

    public State createErrorState() {
        State error = createState();
        setErrorState(error);
        return error;
    }

    private void setErrorState(State errorState) {
        this.errorState = errorState;
        errorState.setLabel(ERROR_LABEL);
    }

    public State getErrorState() {
        return this.errorState;
    }

	public Transition createTransition(State sourceState, State target, String action, String reliability) {
	    
		if (stateIsNull(sourceState)) {
	        return null;
	    }
		
	    List<Transition> listOfTransitions = createListOfTransitions(sourceState);

		Transition newTransition = new Transition(sourceState, target, action, reliability);
		
		return verifySucess(sourceState, listOfTransitions, newTransition);
	}

	private Transition verifySucess(State sourceState, List<Transition> listOfTransitions, Transition newTransition) {
		boolean success = listOfTransitions.add(newTransition);
		getTransitionSystem().put(sourceState, listOfTransitions);
		return success ? newTransition : null;
	}
	
	
	private List<Transition> createListOfTransitions(State sourceState) {
		
		List<Transition> listOfTransitions = getTransitionSystem().get(sourceState);
	    
		if (transitionIsNull(listOfTransitions)) {
			listOfTransitions = new LinkedList<Transition>();
		}
		return listOfTransitions;
	}

	private boolean stateIsNull(State source) {
		if (source == null) {
	        return true;
		}
		return false; 
	}
	
	
	
	private boolean transitionIsNull(List<Transition> listOfTransitions) {
		if (listOfTransitions == null) {
	        return true;
		}
		return false; 
	}
	

	/**
	 * Creates an explicit interface to another FDTMC.
	 *
	 * An interface is an FDTMC fragment with 3 states (initial, success, and error)
	 * and 2 transitions (initial to success with probability {@code id} and initial
	 * to error with probability 1 - {@code id}).
	 *
	 * @param id Identifier of the FDTMC to be abstracted away.
	 * @param initial Initial state of the interface.
	 * @param success Success state of the interface.
	 * @param error Error state of the interface.
	 */
	public Interface createInterface(String id, State initial, State success, State error) {
	    Transition successTransition = createTransition(initial, success, "", id);
	    Transition errorTransition = createTransition(initial, error, "", "1 - " + id);
	    Interface newInterface = new Interface(id,
	                                           initial,
	                                           success,
	                                           error,
	                                           successTransition,
	                                           errorTransition);
	    
	    addToInterfacesOcurrences(newInterface);

	    return newInterface;
	}
	
	public void addToInterfacesOcurrences(Interface interfaceToBeAdd){
		final String id = interfaceToBeAdd.getAbstractedId();
		List<Interface> interfaceOccurrences = null;
	    if (getInterfaces().containsKey(id)) {
	        interfaceOccurrences = getInterfaces().get(id);
	    } else {
	        interfaceOccurrences = new LinkedList<Interface>();
	        getInterfaces().put(id, interfaceOccurrences);
	    }
	    interfaceOccurrences.add(interfaceToBeAdd);
	}

	public State getStateByLabel(String label) {
		Iterator <State> it = getStates().iterator();
		while (it.hasNext()){
			State s = it.next();
			if (s.getLabel().equals(label))
				return s;
		}
		return null;
	}

	public Transition getTransitionByActionName(String action) {
		Iterator<List<Transition>> iteratorStateAdjacencies = getTransitionSystem().values().iterator();
		
		while (iteratorStateAdjacencies.hasNext()) {
			Iterator <Transition> iteratorTransitions = iteratorStateAdjacencies.next().iterator();
			
			while (iteratorTransitions.hasNext()) {
				Transition transition = iteratorTransitions.next();
				if (transition.getActionName().equals(action))
					return transition;
			}
		}
		return null;
	}


	@Override
	public String toString() {
		String msg = new String();
		Iterator <State> itStates = getKeySetOfStates().iterator();
		
		while (itStates.hasNext()) {
			State state = itStates.next();
			List<Transition> transitionList = this.getTransitionSystem().get(state);
			if (transitionList != null) {
				Iterator <Transition> itTransitions = transitionList.iterator();
				while (itTransitions.hasNext()) {
					Transition transition = itTransitions.next();
					msg = getMessage(msg, state, transition);
				}
			}
		}
		return msg;
	}

	private String getMessage(String msg, State state, Transition transition) {
		msg += state.getVariableName() + "=" + state.getIndex() + ((state.getLabel() != null) ? "(" + state.getLabel() + ")" : "") +
				" --- " + transition.getActionName() + " / " + transition.getProbability() +
				" ---> " + transition.getTarget().getVariableName() + "=" + transition.getTarget().getIndex() + ((transition.getTarget().getLabel() != null) ? "(" + transition.getTarget().getLabel() + ")" : "") + "\n";
		return msg;
	}

	private Set<State> getKeySetOfStates() {
		Set<State> tmpStates = this.getTransitionSystem().keySet();
		return tmpStates;
	}

	/**
	 * Two FDTMCs are deemed equal whenever:
	 *     - their states are equal;
	 *     - their initial, success, and error states are equal;
	 *     - the transitions with concrete values are equal;
	 *     - the transitions with variable names have equal source and target states; and
	 *     - the abstracted interfaces are equal.
	 */
	@Override
	public boolean equals(Object obj) {
	    if (isNotNullAndIsIstanceObject(obj)) {
	        FDTMC other = (FDTMC) obj;
	        LinkedList<List<Interface>> thisInterfaces = new LinkedList<List<Interface>>(getInterfaces().values());
            LinkedList<List<Interface>> otherInterfaces = new LinkedList<List<Interface>>(other.getInterfaces().values());
            
            final boolean equalStates = getStates().equals(other.getStates());
            final boolean equalInitialState = getInitialState().equals(other.getInitialState());
            final boolean equalSucessState = getSuccessState().equals(other.getSuccessState());
            final boolean equalErrorState = getErrorState().equals(other.getErrorState());
            final boolean equalTrasitionSystem = getTransitionSystem().equals(other.getTransitionSystem());
            final boolean equalInterfaces = thisInterfaces.equals(otherInterfaces);
            
            return equalStates && equalInitialState && equalSucessState && equalErrorState && equalTrasitionSystem && equalInterfaces;
	               
	    }
	    return false;
	}
	
	public boolean isNotNullAndIsIstanceObject(Object obj){
		return (obj != null && obj instanceof FDTMC);
	}

	@Override
    public int hashCode() {
        return getStates().hashCode() + getTransitionSystem().hashCode() + getInterfaces().hashCode();
    }

    public Map<State, List<Transition>> getTransitions() {
		return transitionSystem;
	}

	/**
	 * Inlines the given FDTMCs whenever there is an interface corresponding
	 * to the string in the respective index.
	 *
	 * @param indexedModels
	 * @return a new FDTMC which represents this one with the ones specified
	 *         in {@code indexedModels} inlined.
	 */
    public FDTMC inline(Map<String, FDTMC> indexedModels) {
        FDTMC inlined = new FDTMC();
        Map<State, State> statesMapping = copyForInlining(inlined);
        
        inlined =  getFdtmcInline().inline(indexedModels, statesMapping, inlined, this);
        
        return inlined;
    }

    /**
     * Returns a copy of this FDTMC decorated with "presence transitions",
     * i.e., a new initial state with a transition to the original initial
     * state parameterized by the {@code presenceVariable} and a complement
     * transition ({@code 1 - presenceVariable}) to the success state
     * ("short-circuit").
     *
     * @param presenceVariable
     * @return
     */
    public FDTMC decoratedWithPresence(String presenceVariable) {
        FDTMC decorated = copy();

        State oldInitial = decorated.getInitialState();
        State newInitial = decorated.createInitialState();
        // Enter the original chain in case of presence
        decorated.createTransition(newInitial,
                                   oldInitial,
                                   "",
                                   presenceVariable);
        // Short-circuit in case of absence
        decorated.createTransition(newInitial,
                                   decorated.getSuccessState(),
                                   "",
                                   "1-"+presenceVariable);
        return decorated;
    }

    /**
     * Returns an FDTMC with a transition to {@code ifPresent} annotated by
     * {@code presenceVariable} and a complement one ({@code 1 - ifPresent})
     * to {@code ifAbsent}. Of course, {@code presenceVariable} is meant to
     * be resolved with a value of 0 or 1.
     *
     * The success states of both {@code ifPresent} and {@code ifAbsent} are
     * linked to a new success state.
     *
     * @param presenceVariable
     * @param ifPresent
     * @param ifAbsent
     * @return
     */
    public static FDTMC ifThenElse(String presenceVariable, FDTMC ifPresent, FDTMC ifAbsent) {
        // TODO Handle ifAbsent.
        return ifPresent.decoratedWithPresence(presenceVariable);
    }

    /**
     * Prepares {@code destination} FDTMC to be an inlined version of this one.
     * @param destination
     * @return a mapping from states in this FDTMC to the corresponding states
     *      in the copied one ({@code destination}).
     */
    protected Map<State, State> copyForInlining(FDTMC destination) {
        destination.setVariableName(this.getVariableName());

        Map<State, State> statesMapping = destination.inlineStates(this);
        destination.setInitialState(statesMapping.get(this.getInitialState()));
        destination.setSuccessState(statesMapping.get(this.getSuccessState()));
        destination.setErrorState(statesMapping.get(this.getErrorState()));

        destination.inlineTransitions(this, statesMapping);
        return statesMapping;
    }

    /**
     * Copies this FDTMC.
     * @return a new FDTMC which is a copy of this one.
     */
    private FDTMC copy() {
        FDTMC copied = new FDTMC();
        copied.setVariableName(this.getVariableName());

        Map<State, State> statesMapping = copied.inlineStates(this);
        copied.setInitialState(statesMapping.get(this.getInitialState()));
        copied.setSuccessState(statesMapping.get(this.getSuccessState()));
        copied.setErrorState(statesMapping.get(this.getErrorState()));

        copied.inlineTransitions(this, statesMapping);
        copied.inlineInterfaces(this, statesMapping);
        return copied;
    }

    /**
     * Inlines all states from {@code fdtmc} stripped of their labels.
     * @param fdtmc
     * @return
     */
    protected Map<State, State> inlineStates(FDTMC fdtmc) {
        
        return getFdtmcInline().inlineStates(fdtmc, this);
    }

    /**
     * Inlines all transitions from {@code fdtmc} that are not part of an interface.
     *
     * @param fdtmc
     * @param statesOldToNew
     */
    protected void inlineTransitions(FDTMC fdtmc, Map<State, State> statesOldToNew) {
    	getFdtmcInline().inlineTransitions(fdtmc, statesOldToNew, this);
    }

    /**
     * Inlines all interfaces (and respective transitions) from {@code fdtmc}
     * into this one.
     *
     * @param fdtmc
     * @param statesOldToNew
     */
    protected void inlineInterfaces(FDTMC fdtmc, Map<State, State> statesOldToNew) {
    	getFdtmcInline().inlineInterfaces(fdtmc, statesOldToNew, this);
    }


    protected Set<Transition> getInterfaceTransitions() {
        Set<Transition> transitions = new HashSet<Transition>();
        getInterfaces().values().stream().flatMap(List<Interface>::stream)
                .forEach(iface -> {
                    transitions.add(iface.getSuccessTransition());
                    transitions.add(iface.getErrorTransition());
                });
        return transitions;
    }

}
