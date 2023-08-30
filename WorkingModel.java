package InitialModel;

import HAL.GridsAndAgents.AgentGrid2D;
import HAL.GridsAndAgents.PDEGrid2D;
import HAL.Gui.GridWindow;
import HAL.GridsAndAgents.AgentSQ2Dunstackable;
import HAL.Tools.FileIO;
import HAL.Rand;
import HAL.Util;

import static InitialModel.ExampleModel.*;
import static HAL.Util.*;
import java.util.Arrays;
import static java.lang.Math.max;

public class ExampleModel extends AgentGrid2D<InitialModel.ExampleCell> {
    //model constants
    public final static int RESISTANT = RGB(1, 0, 0), SENSITIVE = RGB(0, 1, 0);
    public double TIMESTEP = 1.0 / 24;//1 hour per timestep
    public double SPACE_STEP = 20;//um
    public double DIV_PROB_SEN = ProbScale(77.0/1350, TIMESTEP);
    public double DIV_PROB_RES = ProbScale(77.0/1350, TIMESTEP);
    public double DEATH_PROB = ProbScale(0.01, TIMESTEP);
    public double DRUG_DEATH = ProbScale(0.2, TIMESTEP);
    public double DRUG_START = 0 / TIMESTEP;
    public double DRUG_PERIOD = 15 / TIMESTEP;
    public double DRUG_DURATION = 2 / TIMESTEP;
    public double DRUG_UPTAKE = -0.1 * TIMESTEP;
    //public double DRUG_DECAY = -0.1 * TIMESTEP;
    public double DRUG_DECAY = -0.5 * TIMESTEP;
    public double DRUG_DIFF_RATE = 0.02 * 60 * 60 * 24 * (TIMESTEP / (SPACE_STEP * SPACE_STEP));
    public double DRUG_BOUNDARY_VAL = 1.0;
    public double DRUG_INPUT_VAL = 0.002;

    public double ACID_DIFF_RATE = 0.2 * 60 * 60 * 24 * (TIMESTEP / (SPACE_STEP * SPACE_STEP));
    public double ACID_SECRETION_RATE = 0.01;
    public double ACID_UPTAKE = -0.1 * TIMESTEP;
   //public double ACID_DECAY = -0.1 * TIMESTEP;
    public double ACID_DECAY = -0.1 * TIMESTEP;
    public double ACID_EFFECT = 0.02;
    //public double ACID_EFFECT = (77.0/1350)-(797.0/14850);
    public double ACID_EFFECT_DIV_PROP = 1.0;
    //Above is the effect that acid has on the chance of division (1.0 is large effect 0.0 is no effect)
    public double ACID_RESISTANCE_EFFECT_DEATH = 0.7;
    public double ACID_RESISTANCE_EFFECT_DIV = 0.7;
    //internal model objects
    public PDEGrid2D drug;
    public PDEGrid2D acid;
    public Rand rng;
    public int[] divHood = MooreHood(false);

    public ExampleModel(int xDim, int yDim, Rand rng) {
        super(xDim, yDim, InitialModel.ExampleCell.class);
        this.rng = rng;
        drug = new PDEGrid2D(xDim, yDim);
        setAcid(new PDEGrid2D(xDim, yDim));
    }

    public static void main(String[] args) {
        //setting up starting constants and data collection
        //int x = 80, y = 80, visScale = 5, tumorRad = 10, msPause = 5, initial_no_cells = 100;
        int x = 80, y = 80, visScale = 5, initial_no_cells = 100, msPause = 5;
        double resistantProb = 0.5;

        int no_res = 0;
        int [] specific_store = new int[3];
        int[] res_store = new int[3];
        int no_sus;
        int [] sus_store = new int[3];

        GridWindow win = new GridWindow("Competitive Release", x * 3, y+1, visScale);
        FileIO popsOut = new FileIO("populations.csv", "w");
        //setting up models
        ExampleModel[] models = new ExampleModel[3];
        for (int i = 0; i < models.length; i++) {
            models[i] = new ExampleModel(x, y, new Rand(1));
            //models[i].InitTumor(tumorRad, resistantProb);
            models[i].InitCells(x, initial_no_cells, resistantProb);
            for (int j =0; j < 3*x; j++) {
                win.SetPix(j, 0, Util.CategorialColor(6));
                win.SetPix(j, y, Util.CategorialColor(6));
            }
            for (int j =0; j < x; j++) {
                win.SetPix(0, j, Util.CategorialColor(6));
                win.SetPix(x-1, j, Util.CategorialColor(6));
                win.SetPix(2*x-1, j, Util.CategorialColor(6));
                win.SetPix(3*x-1, j, Util.CategorialColor(6));
            }
        }
        models[0].DRUG_DURATION = 0;//no drug
        models[1].DRUG_DURATION = 40000;//constant drug
        //Main run loop
        for (int tick = 0; tick < 40000; tick++) {
            win.TickPause(msPause);
            for (int i = 0; i < models.length; i++) {
                int adaptive;
                if (i ==2){
                    adaptive = 1;
                }
                else{
                    adaptive =0;
                }
                models[i].ModelStep(tick, adaptive, initial_no_cells, x, y);
                models[i].DrawModel(win, i, no_res, specific_store);
                //specific_store
                int res_value;
                res_value = specific_store[i];
                res_store[i] = res_value;

                no_sus = models[i].Pop() - res_value;
                sus_store[i] = no_sus;

                no_res = 0;
                for (int j =0; j < 3*x; j++) {
                    win.SetPix(j, 0, Util.CategorialColor(6));
                    win.SetPix(j, y, Util.CategorialColor(6));
                }
                for (int j =0; j < x; j++) {
                    win.SetPix(0, j, Util.CategorialColor(6));
                    win.SetPix(x-1, j, Util.CategorialColor(6));
                    win.SetPix(2*x-1, j, Util.CategorialColor(6));
                    win.SetPix(3*x-1, j, Util.CategorialColor(6));
                }
            }
            //data recording


            popsOut.Write(models[0].Pop() + "," + res_store[0] + "," + sus_store[0]
                    + "," + models[1].Pop() + "," + res_store[1] + "," + sus_store[1]
                    + "," + models[2].Pop() + "," + res_store[2] + "," + sus_store[2] + "\n");

            //popsOut.Write(models[0].Pop() + "," + models[1].Pop() + "," + models[2].Pop() + "\n");
            if (tick % (int) (10 / models[0].TIMESTEP) == 0) {
                win.ToPNG("ModelsDay" + tick * models[0].TIMESTEP + ".png");
            }
        }
        //closing data collection
        popsOut.Close();
        win.Close();
    }

    public void InitTumor(double radius, double resistantProb) {
        //get a list of indices that fill a circle at the center of the grid
        int[] tumorNeighborhood = CircleHood(true, radius);
        int hoodSize = MapHood(tumorNeighborhood, xDim / 2, yDim / 2);
        for (int i = 0; i < hoodSize; i++) {
            if (rng.Double() < resistantProb) {
                NewAgentSQ(tumorNeighborhood[i]).type = RESISTANT;
            } else {
                NewAgentSQ(tumorNeighborhood[i]).type = SENSITIVE;
            }
        }
    }

    public void InitCells(int x, int initial_no_cells, double resistantProb) {
        //get a list of indices that fill a circle at the center of the grid
        int[] tumorNeighborhood = CircleHood(true, x/2);
        int hoodSize = MapHood(tumorNeighborhood, xDim / 2, yDim / 2);

        int[] location_array = new int[initial_no_cells];
        for (int i =0; i < initial_no_cells; i++){
            int possible_loc;
                for (int j = 0; j < initial_no_cells; j++) {
                    possible_loc = rng.Int(hoodSize);
                    for (int k =0; k < initial_no_cells; k++){
                        if (location_array[k]== possible_loc) {
                            possible_loc = rng.Int(hoodSize);
                            break;
                        }
                    }
                    location_array[i] = possible_loc;
                }
        }

        for (int i = 0; i < initial_no_cells; i++) {
            int location;
            location = location_array[i];
            if (rng.Double() < resistantProb) {
                NewAgentSQ(tumorNeighborhood[location]).type = RESISTANT;
            } else {
                NewAgentSQ(tumorNeighborhood[location]).type = SENSITIVE;
            }
        }
    }

    public void ModelStep(int tick, int adaptive, int initial_no_cells, int x, int y) {
        ShuffleAgents(rng);
        for (InitialModel.ExampleCell cell : this) {
            cell.CellStep();
        }
        //acid diffuses
        getAcid().DiffusionADI(ACID_DIFF_RATE);

        //drug and acid will be removed by natural decay from every location
//        for (int i = 0; i < 6400; i++){
//            if(drug.Get(i) > DRUG_DECAY) {
//                drug.Add(i, DRUG_DECAY);
//            }
//            else{
//                drug.Mul(i,0);
//            }
//            if(acid.Get(i) > ACID_DECAY) {
//                acid.Add(i, ACID_DECAY);
//            }
//            else{
//                acid.Mul(i,0);
//            }
//        }

        //drug and acid will be removed by natural decay from every location
        for (int i = 0; i < x*y; i++){
            drug.Mul(i, DRUG_DECAY);
            acid.Mul(i, ACID_DECAY);
        }
        acid.Update();
        drug.Update();

        if (adaptive == 1){
            if (Pop() > 2*initial_no_cells && (tick - DRUG_START) > 0){
                //drug diffuses
                drug.DiffusionADI(DRUG_DIFF_RATE);

                //drug will be added to every location by "pouring"
                for (int i = 0; i < x*y; i++){
                    drug.Add(i,DRUG_INPUT_VAL);
                }
            }
            else {
                //drug will not enter through boundaries just diffuses
                drug.DiffusionADI(DRUG_DIFF_RATE);
            }
        }
        else{
            double periodTick = (tick - DRUG_START) % DRUG_PERIOD;
            if (periodTick > 0 && periodTick < DRUG_DURATION) {
                //drug will enter through boundaries
                //drug.DiffusionADI(DRUG_DIFF_RATE, DRUG_BOUNDARY_VAL);

                //drug diffuses
                drug.DiffusionADI(DRUG_DIFF_RATE);

                //drug will be added to every location by "pouring"
                for (int i = 0; i < x*y; i++){
                    drug.Add(i,DRUG_INPUT_VAL);
                }
            }
            else {
                //drug will not enter through boundaries just diffuses
                drug.DiffusionADI(DRUG_DIFF_RATE);
            }
        }
        drug.Update();
    }

    public void DrawModel(GridWindow vis, int iModel, int counter, int[] store) {
        for (int x = 0; x < xDim; x++) {
            for (int y = 0; y < yDim; y++) {
                InitialModel.ExampleCell drawMe = GetAgent(x, y);
                if (drawMe != null) {
                    vis.SetPix(x + iModel * xDim, y, drawMe.type);
                    if (drawMe.type == RESISTANT){
                        counter += 1;
                        store[iModel] = counter;
                    }
                } else {
                    // Acid concentration plotted
                    vis.SetPix(x + iModel * xDim, y, HeatMapBGR(acid.Get(x, y)));
                    // Drug concentration plotted
                    //vis.SetPix(x + iModel * xDim, y, HeatMapBGR(drug.Get(x, y)));
                }
            }
        }
    }

    public PDEGrid2D getAcid() {
        return acid;
    }

    public void setAcid(PDEGrid2D acid_input) {
        this.acid = acid_input;
    }


}
class ExampleCell extends AgentSQ2Dunstackable<ExampleModel> {
    public int type;

    public void CellStep() {


        //uptake of Acid
        // N/A
        G.acid.Mul(Isq(), G.ACID_UPTAKE);

        double deathProb, divProb;
        //Chance of Death, depends on resistance, drug concentration and acid concentration
        if (this.type == RESISTANT) {
            deathProb = G.DEATH_PROB + G.getAcid().Get(Isq()) * G.ACID_EFFECT * G.ACID_RESISTANCE_EFFECT_DEATH;
        } else {
            //uptake of Drug
            G.drug.Mul(Isq(), G.DRUG_UPTAKE);
            //Death due to drug and acid
            deathProb = G.DEATH_PROB + G.drug.Get(Isq()) * G.DRUG_DEATH + G.getAcid().Get(Isq()) * G.ACID_EFFECT;
        }
        if (G.rng.Double() < deathProb) {
            Dispose();
            return;
        }
        //Chance of Division, depends on resistance
        //PDEGrid2D acid_input;
        double acid_local, acid_loc;
        if (this.type == RESISTANT) {
            //Chance of division affected by concentration of acid
            divProb = G.DIV_PROB_RES - G.getAcid().Get(Isq()) * G.ACID_EFFECT * G.ACID_EFFECT_DIV_PROP * G.ACID_RESISTANCE_EFFECT_DIV;

            //Acid produced at every timestep from each resistant cell
            acid_local = G.getAcid().Get(Isq()) + G.ACID_SECRETION_RATE;
            G.acid.Set(Isq(), acid_local);

        } else {
            divProb = G.DIV_PROB_SEN - G.getAcid().Get(Isq()) * G.ACID_EFFECT * G.ACID_EFFECT_DIV_PROP;
        }
        if (G.rng.Double() < divProb) {
            int options = MapEmptyHood(G.divHood);
            if (options > 0) {
                G.NewAgentSQ(G.divHood[G.rng.Int(options)]).type = this.type;
            }
        }
    }
}
       
