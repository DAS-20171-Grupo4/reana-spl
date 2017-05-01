package fdtmc;

public class Transition {

	private String actionName;
	private String probability;
	private State source, target;

	public Transition(State source, State target, String actionName, String probability) {
		this.source = source;
		this.target = target;
		this.actionName = actionName;
		this.probability = probability;
	}

	public String getActionName() {
		return actionName;
	}

	public String getProbability() {
		return probability;
	}

	public State getSource() {
		return source;
	}

	public State getTarget() {
		return target;
	}

    /**
     * Two transitions are equal if they have equal source and target states.
     * Moreover, their transition probabilities must be equal numbers or
     * be both (not necessarily equal) variable names.
     */
    @Override
    public boolean equals(Object obj) {
        if (isNotNullAndIsTransition(obj)) {
            Transition other = (Transition) obj;
            return compareTransitions(other);
        }
        return false;
    }

    private boolean isNotNullAndIsTransition(Object obj){
        return obj != null && obj instanceof Transition;
    }

    private boolean compareTransitions(Transition other){
        boolean equalSources = getSource().equals(other.getSource());
        boolean equalTargets = getTarget().equals(other.getTarget());
        boolean equalProbability =	areEqualProbabilities(getProbability(), other.getProbability());
        return equalSources && equalTargets && equalProbability;
    }

    @Override
    public int hashCode() {
        return getSource().hashCode() + getTarget().hashCode();
    }

    /**
     * Returns true if {@code p1} and {@code p2} are equal double values.
     * If they contain variable names, even different ones, the result is also true.
     * @param p1
     * @param p2
     * @return
     */
    private boolean areEqualProbabilities(String p1, String p2) {
        double prob1 = 0;
        double prob2 = 0;
        boolean isVariable = false;
        try {
            prob1 = Double.parseDouble(p1);
        } catch (NumberFormatException e) {
            isVariable = true;
        }
        try {
            prob2 = Double.parseDouble(p2);
        } catch (NumberFormatException e) {
            if (isVariable) {
                return true;
            }
        }
        return prob1 == prob2;
    }
}
