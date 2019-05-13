package com.iscas.strategy;

import com.iscas.bean.Trace;
import com.microsoft.z3.*;

import java.util.*;

public class SAT {
    private final Context ctx;

    public SAT() {
        this.ctx = new Context();
    }

    public List<String[]> sat(List<Trace> traces) {
        Map<String, BoolExpr> consts = mkConsts(traces);
        BoolExpr constraint = mkConstraint(traces, consts);
        Solver solver = ctx.mkSimpleSolver();
        solver.add(constraint);

        List<String[]> res = new LinkedList<>();
        while (solver.check().equals(Status.SATISFIABLE)) {
            Model model = solver.getModel();

            Set<String> resolution = new HashSet<>();
            List<BoolExpr> exclusion = new LinkedList<>();
            for (Map.Entry<String, BoolExpr> entry : consts.entrySet()) {
                Expr tmp = model.getConstInterp(entry.getValue());
                if (tmp != null && tmp.isTrue()) {
                    resolution.add(entry.getKey());
                    exclusion.add(entry.getValue());
                }
            }
            if (!resolution.isEmpty()) {
                res.add(resolution.toArray(new String[0]));
                BoolExpr exConstraint = this.ctx.mkNot(mkConjunction(exclusion));
                solver.add(exConstraint);
            }
        }
        return res;
    }

    private BoolExpr mkConstraint(List<Trace> traces, Map<String, BoolExpr> consts) {
        List<BoolExpr> disjunctions = new LinkedList<>();
        for (Trace trace : traces) {
            List<BoolExpr> bools = new LinkedList<>();
            String[] svcs = trace.getServices();
            for (String name : svcs)
                bools.add(consts.get(name));
            BoolExpr disjunction = mkDisjunction(bools);
            disjunctions.add(disjunction);
        }
        return mkConjunction(disjunctions);
    }

    private Map<String, BoolExpr> mkConsts(List<Trace> traces) {
        Set<String> svcs = new HashSet<>();
        for (Trace t : traces) {
            svcs.addAll(Arrays.asList(t.getServices()));
        }
        Map<String, BoolExpr> consts = new HashMap<>();
        for (String svc : svcs) {
            BoolExpr expr = ctx.mkBoolConst(svc);
            consts.put(svc, expr);
        }
        return consts;
    }

    private BoolExpr mkConjunction(List<BoolExpr> exprs) {
        BoolExpr res = ctx.mkBool(true);
        for (BoolExpr expr : exprs)
            res = ctx.mkAnd(res, expr);
        return res;
    }

    private BoolExpr mkDisjunction(List<BoolExpr> exprs) {
        BoolExpr res = ctx.mkBool(false);
        for (BoolExpr expr : exprs)
            res = ctx.mkOr(res, expr);
        return res;
    }
}
